package co.edu.monteria.medicitas.application.service;

import co.edu.monteria.medicitas.application.ports.out.AutorizacionAccesoRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.HistoriaClinicaRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.IpsRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.MedicoRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.NotificacionPort;
import co.edu.monteria.medicitas.application.ports.out.UsuarioRepositoryPort;
import co.edu.monteria.medicitas.domain.exception.AccesoDenegadoHistoriaException;
import co.edu.monteria.medicitas.domain.exception.AccesoNoAutorizadoException;
import co.edu.monteria.medicitas.domain.model.AutorizacionAcceso;
import co.edu.monteria.medicitas.domain.model.HistoriaClinica;
import co.edu.monteria.medicitas.domain.model.Ips;
import co.edu.monteria.medicitas.domain.model.Medico;
import co.edu.monteria.medicitas.domain.model.Usuario;
import co.edu.monteria.medicitas.domain.model.enums.EstadoAutorizacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas del cumplimiento de la RES. 1995/1999: acceso a la historia clínica entre IPS
 * con consentimiento explícito del paciente.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HistoriaClinicaService — reserva legal y consentimiento entre IPS")
class HistoriaClinicaServiceTest {

    @Mock
    private HistoriaClinicaRepositoryPort historiaClinicaRepository;
    @Mock
    private AutorizacionAccesoRepositoryPort autorizacionAccesoRepository;
    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @Mock
    private IpsRepositoryPort ipsRepository;
    @Mock
    private MedicoRepositoryPort medicoRepository;
    @Mock
    private NotificacionPort notificacionPort;

    @InjectMocks
    private HistoriaClinicaService servicio;

    private static final Long PACIENTE = 1L;
    private static final Long MEDICO_USUARIO = 2L;
    private static final Long IPS_ORIGEN = 100L;
    private static final Long IPS_SOLICITANTE = 200L;

    private HistoriaClinica historia;

    @BeforeEach
    void setUp() {
        historia = new HistoriaClinica();
        historia.setId(500L);
        historia.setPaciente(usuario(PACIENTE, "Carlos Mendoza"));
        historia.setIpsOrigen(ips(IPS_ORIGEN, "IPS Salud Vital"));
        historia.setDiagnostico("Hipertensión arterial primaria");
        historia.setContenido("Cifras tensionales 135/85 mmHg");
    }

    @Test
    @DisplayName("la IPS de origen accede a su propia custodia sin necesidad de autorización")
    void ipsDeOrigenAccedeSinAutorizacion() {
        when(historiaClinicaRepository.buscarPorPacienteId(PACIENTE)).thenReturn(List.of(historia));
        when(medicoRepository.buscarPorUsuarioId(MEDICO_USUARIO))
                .thenReturn(Optional.of(medicoDe(IPS_ORIGEN)));

        List<HistoriaClinica> resultado =
                servicio.consultarHistoriasDesdeIps(MEDICO_USUARIO, PACIENTE, IPS_ORIGEN);

        assertThat(resultado).hasSize(1);
        verify(autorizacionAccesoRepository, never()).buscarPorHistoriaEIpsSolicitante(any(), any());
    }

    @Test
    @DisplayName("una IPS externa SIN autorización del paciente recibe 403, no una lista vacía")
    void ipsExternaSinAutorizacionRecibe403() {
        when(historiaClinicaRepository.buscarPorPacienteId(PACIENTE)).thenReturn(List.of(historia));
        when(medicoRepository.buscarPorUsuarioId(MEDICO_USUARIO))
                .thenReturn(Optional.of(medicoDe(IPS_SOLICITANTE)));
        when(autorizacionAccesoRepository.buscarPorHistoriaEIpsSolicitante(500L, IPS_SOLICITANTE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                servicio.consultarHistoriasDesdeIps(MEDICO_USUARIO, PACIENTE, IPS_SOLICITANTE))
                .isInstanceOf(AccesoDenegadoHistoriaException.class)
                .hasMessageContaining("1995");
    }

    @Test
    @DisplayName("una autorización REVOCADA también bloquea el acceso")
    void autorizacionRevocadaBloqueaAcceso() {
        when(historiaClinicaRepository.buscarPorPacienteId(PACIENTE)).thenReturn(List.of(historia));
        when(medicoRepository.buscarPorUsuarioId(MEDICO_USUARIO))
                .thenReturn(Optional.of(medicoDe(IPS_SOLICITANTE)));
        when(autorizacionAccesoRepository.buscarPorHistoriaEIpsSolicitante(500L, IPS_SOLICITANTE))
                .thenReturn(Optional.of(autorizacion(EstadoAutorizacion.REVOCADO)));

        assertThatThrownBy(() ->
                servicio.consultarHistoriasDesdeIps(MEDICO_USUARIO, PACIENTE, IPS_SOLICITANTE))
                .isInstanceOf(AccesoDenegadoHistoriaException.class);
    }

    @Test
    @DisplayName("con autorización HABILITADA la IPS externa sí accede")
    void autorizacionHabilitadaPermiteAcceso() {
        when(historiaClinicaRepository.buscarPorPacienteId(PACIENTE)).thenReturn(List.of(historia));
        when(medicoRepository.buscarPorUsuarioId(MEDICO_USUARIO))
                .thenReturn(Optional.of(medicoDe(IPS_SOLICITANTE)));
        when(autorizacionAccesoRepository.buscarPorHistoriaEIpsSolicitante(500L, IPS_SOLICITANTE))
                .thenReturn(Optional.of(autorizacion(EstadoAutorizacion.HABILITADO)));

        List<HistoriaClinica> resultado =
                servicio.consultarHistoriasDesdeIps(MEDICO_USUARIO, PACIENTE, IPS_SOLICITANTE);

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("un médico no puede consultar haciéndose pasar por otra IPS")
    void medicoNoPuedeUsarUnaIpsQueNoEsSuya() {
        when(medicoRepository.buscarPorUsuarioId(MEDICO_USUARIO))
                .thenReturn(Optional.of(medicoDe(IPS_ORIGEN)));

        assertThatThrownBy(() ->
                servicio.consultarHistoriasDesdeIps(MEDICO_USUARIO, PACIENTE, IPS_SOLICITANTE))
                .isInstanceOf(AccesoNoAutorizadoException.class)
                .hasMessageContaining("adscripción");

        verify(historiaClinicaRepository, never()).buscarPorPacienteId(any());
    }

    @Test
    @DisplayName("solo el titular del registro puede autorizar el acceso a su historia")
    void soloElTitularAutoriza() {
        when(historiaClinicaRepository.buscarPorId(500L)).thenReturn(Optional.of(historia));

        assertThatThrownBy(() ->
                servicio.autorizarAccesoIps(500L, IPS_SOLICITANTE, 999L))
                .isInstanceOf(AccesoNoAutorizadoException.class);

        verify(autorizacionAccesoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("el paciente sin historial no recibe 403: no hay nada que proteger")
    void pacienteSinHistorialNoRecibe403() {
        when(historiaClinicaRepository.buscarPorPacienteId(PACIENTE)).thenReturn(List.of());
        when(medicoRepository.buscarPorUsuarioId(MEDICO_USUARIO))
                .thenReturn(Optional.of(medicoDe(IPS_SOLICITANTE)));

        List<HistoriaClinica> resultado =
                servicio.consultarHistoriasDesdeIps(MEDICO_USUARIO, PACIENTE, IPS_SOLICITANTE);

        assertThat(resultado).isEmpty();
    }

    // ------------------------------------------------------------------

    private Usuario usuario(Long id, String nombre) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombreCompleto(nombre);
        return u;
    }

    private Ips ips(Long id, String nombre) {
        Ips i = new Ips();
        i.setId(id);
        i.setNombre(nombre);
        return i;
    }

    private Medico medicoDe(Long ipsId) {
        Medico m = new Medico();
        m.setId(1L);
        m.setIps(ips(ipsId, "IPS"));
        return m;
    }

    private AutorizacionAcceso autorizacion(EstadoAutorizacion estado) {
        AutorizacionAcceso a = new AutorizacionAcceso();
        a.setId(1L);
        a.setHistoria(historia);
        a.setEstado(estado);
        return a;
    }
}
