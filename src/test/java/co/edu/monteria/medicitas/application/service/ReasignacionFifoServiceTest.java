package co.edu.monteria.medicitas.application.service;

import co.edu.monteria.medicitas.application.ports.in.ReasignacionFifoUseCase;
import co.edu.monteria.medicitas.application.ports.out.CitaRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.ColaEsperaRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.DisponibilidadRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.NotificacionPort;
import co.edu.monteria.medicitas.domain.model.Cita;
import co.edu.monteria.medicitas.domain.model.ColaEspera;
import co.edu.monteria.medicitas.domain.model.DisponibilidadMedico;
import co.edu.monteria.medicitas.domain.model.Especialidad;
import co.edu.monteria.medicitas.domain.model.Ips;
import co.edu.monteria.medicitas.domain.model.Medico;
import co.edu.monteria.medicitas.domain.model.Usuario;
import co.edu.monteria.medicitas.domain.model.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas del algoritmo de REASIGNACIÓN AUTOMÁTICA FIFO.
 *
 * Es la pieza con más lógica de negocio del proyecto y la que más se ha INCORRECTAMENTE
 * descrito antes como "atómica": las pruebas cubren expresamente los dos puntos donde
 * antes fallaba, que son la carrera por la cabecera de la cola y la carrera por el cupo.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReasignacionFifoService — algoritmo de cola FIFO")
class ReasignacionFifoServiceTest {

    @Mock
    private ColaEsperaRepositoryPort colaEsperaRepository;
    @Mock
    private DisponibilidadRepositoryPort disponibilidadRepository;
    @Mock
    private CitaRepositoryPort citaRepository;
    @Mock
    private NotificacionPort notificacionPort;

    private ReasignacionFifoUseCase servicio;
    private final AtomicLong idSeq = new AtomicLong(1000);

    @BeforeEach
    void setUp() {
        servicio = new ReasignacionFifoService(
                colaEsperaRepository, disponibilidadRepository, citaRepository, notificacionPort);
    }

    @Test
    @DisplayName("no hace nada si no hay nadie en la cola de espera")
    void noReasignaSiLaColaEstaVacia() {
        DisponibilidadMedico disponibilidad = disponibilidadLibre(1L);
        when(colaEsperaRepository.obtenerCabeceraFifoBloqueada(anyLong(), anyLong(), any()))
                .thenReturn(Optional.empty());

        boolean reasignado = servicio.reasignarSiHayEspera(disponibilidad);

        assertThat(reasignado).isFalse();
        verify(citaRepository, never()).guardar(any());
        verify(notificacionPort, never()).enviarNotificacion(any(), any(), any(), any());
        // El cupo debe quedar libre para el siguiente paciente que sí pueda agendarlo.
        verify(disponibilidadRepository, never())
                .transicionarEstadoSiCoincide(anyLong(), any(), any());
    }

    @Test
    @DisplayName("asigna el cupo al primero de la cola, lo marca ASIGNADO y le notifica")
    void asignaElCupoAlPrimeroDeLaCola() {
        DisponibilidadMedico disponibilidad = disponibilidadLibre(1L);
        ColaEspera enEspera = colaEnEspera(7L, "Paciente Siete");

        when(colaEsperaRepository.obtenerCabeceraFifoBloqueada(10L, 20L, EstadoColaEspera.EN_ESPERA))
                .thenReturn(Optional.of(enEspera));
        when(disponibilidadRepository.transicionarEstadoSiCoincide(
                1L, EstadoDisponibilidad.LIBRE, EstadoDisponibilidad.OCUPADO))
                .thenReturn(true);
        when(citaRepository.guardar(any(Cita.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        boolean reasignado = servicio.reasignarSiHayEspera(disponibilidad);

        assertThat(reasignado).isTrue();
        assertThat(enEspera.getEstado()).isEqualTo(EstadoColaEspera.ASIGNADO);

        ArgumentCaptor<Cita> citaCaptor = ArgumentCaptor.forClass(Cita.class);
        verify(citaRepository).guardar(citaCaptor.capture());
        Cita citaCreada = citaCaptor.getValue();

        assertThat(citaCreada.getPaciente().getId()).isEqualTo(7L);
        assertThat(citaCreada.getEstado()).isEqualTo(EstadoCita.PROGRAMADA);
        assertThat(citaCreada.getTipo()).isEqualTo(TipoCita.CONSULTA);
        assertThat(citaCreada.getDisponibilidad().getId()).isEqualTo(1L);
        assertThat(citaCreada.getObservaciones()).containsIgnoringCase("Cola de Espera FIFO");

        verify(notificacionPort).enviarNotificacion(
                eq(enEspera.getPaciente()), any(), any(), eq(TipoNotificacion.PUSH));
    }

    @Test
    @DisplayName("no marca a nadie como ASIGNADO si otra transacción se adelantó al cupo")
    void noMarcaLaColaSiElCupoYaFueTomado() {
        DisponibilidadMedico disponibilidad = disponibilidadLibre(1L);
        ColaEspera enEspera = colaEnEspera(7L, "Paciente Siete");

        when(colaEsperaRepository.obtenerCabeceraFifoBloqueada(10L, 20L, EstadoColaEspera.EN_ESPERA))
                .thenReturn(Optional.of(enEspera));
        // Transición condicionada devuelve false: otro paciente ganó la carrera.
        when(disponibilidadRepository.transicionarEstadoSiCoincide(
                1L, EstadoDisponibilidad.LIBRE, EstadoDisponibilidad.OCUPADO))
                .thenReturn(false);

        boolean reasignado = servicio.reasignarSiHayEspera(disponibilidad);

        assertThat(reasignado).isFalse();
        // El punto crítico: la solicitud DEBE seguir en cola para el próximo cupo libre.
        assertThat(enEspera.getEstado()).isEqualTo(EstadoColaEspera.EN_ESPERA);
        verify(citaRepository, never()).guardar(any());
        verify(notificacionPort, never()).enviarNotificacion(any(), any(), any(), any());
    }

    @Test
    @DisplayName("ignora disponibilidades sin médico, especialidad o IPS resueltos")
    void ignoraDisponibilidadesIncompletas() {
        assertThat(servicio.reasignarSiHayEspera(null)).isFalse();
        assertThat(servicio.reasignarSiHayEspera(new DisponibilidadMedico())).isFalse();

        DisponibilidadMedico sinIps = new DisponibilidadMedico();
        sinIps.setId(1L);
        Medico medico = medico();
        medico.setIps(null);
        sinIps.setMedico(medico);

        assertThat(servicio.reasignarSiHayEspera(sinIps)).isFalse();
        verify(colaEsperaRepository, never()).obtenerCabeceraFifoBloqueada(anyLong(), anyLong(), any());
    }

    // ------------------------------------------------------------------
    // Ayudas de construcción
    // ------------------------------------------------------------------

    private DisponibilidadMedico disponibilidadLibre(Long id) {
        DisponibilidadMedico d = new DisponibilidadMedico();
        d.setId(id);
        d.setMedico(medico());
        d.setEstado(EstadoDisponibilidad.LIBRE);
        return d;
    }

    private Medico medico() {
        Medico m = new Medico();
        m.setId(1L);

        Especialidad especialidad = new Especialidad();
        especialidad.setId(10L);
        especialidad.setNombre("Cardiología");
        m.setEspecialidad(especialidad);

        Ips ips = new Ips();
        ips.setId(20L);
        ips.setNombre("Clínica Montería Central");
        m.setIps(ips);

        return m;
    }

    private ColaEspera colaEnEspera(Long pacienteId, String nombre) {
        Usuario paciente = new Usuario();
        paciente.setId(pacienteId);
        paciente.setNombreCompleto(nombre);

        ColaEspera cola = new ColaEspera();
        cola.setId(idSeq.incrementAndGet());
        cola.setPaciente(paciente);
        cola.setEstado(EstadoColaEspera.EN_ESPERA);
        return cola;
    }
}
