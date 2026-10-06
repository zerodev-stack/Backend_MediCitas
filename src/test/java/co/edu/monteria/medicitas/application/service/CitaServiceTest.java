package co.edu.monteria.medicitas.application.service;

import co.edu.monteria.medicitas.application.ports.in.ReasignacionFifoUseCase;
import co.edu.monteria.medicitas.application.ports.out.CitaRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.DisponibilidadRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.MedicoRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.NotificacionPort;
import co.edu.monteria.medicitas.application.ports.out.UsuarioRepositoryPort;
import co.edu.monteria.medicitas.domain.exception.AccesoNoAutorizadoException;
import co.edu.monteria.medicitas.domain.exception.ConflictoConcurrenciaException;
import co.edu.monteria.medicitas.domain.exception.RecursoNoEncontradoException;
import co.edu.monteria.medicitas.domain.exception.ReglaNegocioException;
import co.edu.monteria.medicitas.domain.model.Cita;
import co.edu.monteria.medicitas.domain.model.DisponibilidadMedico;
import co.edu.monteria.medicitas.domain.model.Especialidad;
import co.edu.monteria.medicitas.domain.model.Ips;
import co.edu.monteria.medicitas.domain.model.Medico;
import co.edu.monteria.medicitas.domain.model.Usuario;
import co.edu.monteria.medicitas.domain.model.enums.EstadoCita;
import co.edu.monteria.medicitas.domain.model.enums.EstadoDisponibilidad;
import co.edu.monteria.medicitas.domain.model.enums.TipoCita;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas de propiedad y reserva de cupos en CitaService.
 *
 * El caso 'un paciente no puede cancelar la cita de otro' cubre la vulnerabilidad
 * más grave que tenía el proyecto: el endpoint recibía el 'usuarioId' como parámetro y
 * nunca lo comparaba con el dueño de la cita.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CitaService — propiedad de la cita y reserva atómica del cupo")
class CitaServiceTest {

    @Mock private CitaRepositoryPort citaRepository;
    @Mock private DisponibilidadRepositoryPort disponibilidadRepository;
    @Mock private UsuarioRepositoryPort usuarioRepository;
    @Mock private MedicoRepositoryPort medicoRepository;
    @Mock private NotificacionPort notificacionPort;
    @Mock private ReasignacionFifoUseCase reasignacionFifoUseCase;

    private CitaService servicio;

    private static final Long PACIENTE_DUENO = 1L;
    private static final Long PACIENTE_AJENO = 99L;

    @BeforeEach
    void setUp() {
        servicio = new CitaService(citaRepository, disponibilidadRepository, usuarioRepository,
                medicoRepository, notificacionPort, reasignacionFifoUseCase);
    }

    @Test
    @DisplayName("un paciente NO puede cancelar la cita de otro paciente")
    void noPuedeCancelarCitaAjena() {
        Cita citaDeOtro = citaProgramada(PACIENTE_DUENO, 500L);
        when(citaRepository.buscarPorId(500L)).thenReturn(Optional.of(citaDeOtro));

        assertThatThrownBy(() -> servicio.cancelarCita(500L, PACIENTE_AJENO))
                .isInstanceOf(AccesoNoAutorizadoException.class)
                .hasMessageContaining("cancelar");

        verify(citaRepository, never()).guardar(any());
        verify(reasignacionFifoUseCase, never()).reasignarSiHayEspera(any());
    }

    @Test
    @DisplayName("un paciente NO puede aplazar la cita de otro paciente")
    void noPuedeAplazarCitaAjena() {
        when(citaRepository.buscarPorId(500L))
                .thenReturn(Optional.of(citaProgramada(PACIENTE_DUENO, 500L)));

        assertThatThrownBy(() -> servicio.aplazarCita(500L, 501L, PACIENTE_AJENO))
                .isInstanceOf(AccesoNoAutorizadoException.class);

        verify(disponibilidadRepository, never())
                .transicionarEstadoSiCoincide(anyLong(), any(), any());
    }

    @Test
    @DisplayName("al cancelar, el cupo se libera y se dispara la reasignación FIFO")
    void cancelarLiberaCupoYDisparaFifo() {
        Cita cita = citaProgramada(PACIENTE_DUENO, 500L);
        when(citaRepository.buscarPorId(500L)).thenReturn(Optional.of(cita));
        when(citaRepository.guardar(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        Cita cancelada = servicio.cancelarCita(500L, PACIENTE_DUENO);

        assertThat(cancelada.getEstado()).isEqualTo(EstadoCita.CANCELADA);
        verify(disponibilidadRepository).transicionarEstadoSiCoincide(
                500L, EstadoDisponibilidad.OCUPADO, EstadoDisponibilidad.LIBRE);
        verify(reasignacionFifoUseCase).reasignarSiHayEspera(cita.getDisponibilidad());
    }

    @Test
    @DisplayName("no se puede cancelar dos veces la misma cita")
    void noPuedeCancelarDosVeces() {
        Cita yaCancelada = citaProgramada(PACIENTE_DUENO, 500L);
        yaCancelada.setEstado(EstadoCita.CANCELADA);
        when(citaRepository.buscarPorId(500L)).thenReturn(Optional.of(yaCancelada));

        assertThatThrownBy(() -> servicio.cancelarCita(500L, PACIENTE_DUENO))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("PROGRAMADA");
    }

    @Test
    @DisplayName("si otro paciente ganó la carrera por el cupo, se informa conflicto y no se duplica la cita")
    void conflictoCuandoOtroReservoElCupo() {
        DisponibilidadMedico disponibilidad = disponibilidad(600L, EstadoDisponibilidad.LIBRE, 1L);
        when(usuarioRepository.buscarPorId(PACIENTE_DUENO))
                .thenReturn(Optional.of(usuario(PACIENTE_DUENO)));
        when(disponibilidadRepository.buscarPorId(600L)).thenReturn(Optional.of(disponibilidad));
        when(disponibilidadRepository.transicionarEstadoSiCoincide(
                600L, EstadoDisponibilidad.LIBRE, EstadoDisponibilidad.OCUPADO))
                .thenReturn(false);

        assertThatThrownBy(() -> servicio.agendarCita(PACIENTE_DUENO, 600L, TipoCita.CONSULTA, null))
                .isInstanceOf(ConflictoConcurrenciaException.class);

        verify(citaRepository, never()).guardar(any());
        verify(notificacionPort, never()).enviarNotificacion(any(), any(), any(), any());
    }

    @Test
    @DisplayName("el aplazamiento solo admite franjas de la misma especialidad e IPS")
    void aplazarExigeMismaEspecialidadEIps() {
        Cita cita = citaProgramada(PACIENTE_DUENO, 500L);
        when(citaRepository.buscarPorId(500L)).thenReturn(Optional.of(cita));

        // Disponibilidad de OTRA especialidad: debe rechazarse antes de tocar nada.
        DisponibilidadMedico otraEspecialidad = new DisponibilidadMedico();
        otraEspecialidad.setId(700L);
        otraEspecialidad.setEstado(EstadoDisponibilidad.LIBRE);
        Especialidad otra = new Especialidad();
        otra.setId(999L);
        otra.setNombre("Dermatología");
        Ips ips = new Ips();
        ips.setId(100L);
        Medico medicoOtro = new Medico();
        medicoOtro.setId(2L);
        medicoOtro.setEspecialidad(otra);
        medicoOtro.setIps(ips);
        otraEspecialidad.setMedico(medicoOtro);

        when(disponibilidadRepository.buscarPorId(700L)).thenReturn(Optional.of(otraEspecialidad));

        assertThatThrownBy(() -> servicio.aplazarCita(500L, 700L, PACIENTE_DUENO))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("especialidad o IPS");

        verify(disponibilidadRepository, never())
                .transicionarEstadoSiCoincide(anyLong(), any(), any());
    }

    @Test
    @DisplayName("no se puede aplazar a la misma franja que ya ocupa la cita")
    void noPuedeAplazarALaMismaFranja() {
        Cita cita = citaProgramada(PACIENTE_DUENO, 500L);
        when(citaRepository.buscarPorId(500L)).thenReturn(Optional.of(cita));
        when(disponibilidadRepository.buscarPorId(500L))
                .thenReturn(Optional.of(cita.getDisponibilidad()));

        assertThatThrownBy(() -> servicio.aplazarCita(500L, 500L, PACIENTE_DUENO))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("distinta");
    }

    @Test
    @DisplayName("la agenda del médico se resuelve a partir de su usuario, no de un id de ruta")
    void agendaMedicoSeResuelvePorUsuario() {
        Medico medico = new Medico();
        medico.setId(42L);
        when(medicoRepository.buscarPorUsuarioId(2L)).thenReturn(Optional.of(medico));
        when(citaRepository.buscarPorMedicoId(42L)).thenReturn(List.of());

        assertThat(servicio.obtenerCitasMedico(2L)).isEmpty();
        verify(citaRepository).buscarPorMedicoId(42L);
    }

    @Test
    @DisplayName("un token de médico sin ficha profesional recibe 404, no la agenda de otro")
    void medicoSinFichaRechazado() {
        when(medicoRepository.buscarPorUsuarioId(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtenerCitasMedico(77L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    // ------------------------------------------------------------------

    private Cita citaProgramada(Long pacienteId, Long disponibilidadId) {
        Cita cita = new Cita();
        cita.setId(disponibilidadId);
        cita.setPaciente(usuario(pacienteId));
        cita.setDisponibilidad(disponibilidad(disponibilidadId, EstadoDisponibilidad.OCUPADO, 1L));
        cita.setEstado(EstadoCita.PROGRAMADA);
        return cita;
    }

    private DisponibilidadMedico disponibilidad(Long id, EstadoDisponibilidad estado, Long especialidadId) {
        DisponibilidadMedico d = new DisponibilidadMedico();
        d.setId(id);
        d.setEstado(estado);

        Especialidad especialidad = new Especialidad();
        especialidad.setId(especialidadId);
        especialidad.setNombre("Cardiología");

        Ips ips = new Ips();
        ips.setId(100L);
        ips.setNombre("Clínica Montería Central");

        Medico medico = new Medico();
        medico.setId(1L);
        medico.setEspecialidad(especialidad);
        medico.setIps(ips);
        d.setMedico(medico);
        return d;
    }

    private Usuario usuario(Long id) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombreCompleto("Paciente " + id);
        return u;
    }
}
