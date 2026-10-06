package co.edu.monteria.medicitas.application.service;

import co.edu.monteria.medicitas.application.ports.in.HistoriaClinicaUseCase;
import co.edu.monteria.medicitas.application.ports.out.AutorizacionAccesoRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.HistoriaClinicaRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.IpsRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.MedicoRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.NotificacionPort;
import co.edu.monteria.medicitas.application.ports.out.UsuarioRepositoryPort;
import co.edu.monteria.medicitas.domain.exception.AccesoDenegadoHistoriaException;
import co.edu.monteria.medicitas.domain.exception.AccesoNoAutorizadoException;
import co.edu.monteria.medicitas.domain.exception.RecursoNoEncontradoException;
import co.edu.monteria.medicitas.domain.model.AutorizacionAcceso;
import co.edu.monteria.medicitas.domain.model.HistoriaClinica;
import co.edu.monteria.medicitas.domain.model.Ips;
import co.edu.monteria.medicitas.domain.model.Medico;
import co.edu.monteria.medicitas.domain.model.Usuario;
import co.edu.monteria.medicitas.domain.model.enums.EstadoAutorizacion;
import co.edu.monteria.medicitas.domain.model.enums.TipoNotificacion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de Aplicación para el Manejo y Autorización de Historia Clínica.
 *
 * CUMPLIMIENTO DE MARCO LEGAL EN COLOMBIA (Res. 1995 de 1999 y Res. 3280 de 2018):
 * La historia clínica es un documento privado sometido a reserva legal. Cada IPS donde el
 * paciente recibe atención actúa como custodia del registro generado en esa sede
 * ('ips_origen').
 *
 * Ninguna otra IPS ('ips_solicitante') puede consultar los registros clínicos generados por
 * una IPS distinta a menos que el paciente haya otorgado una autorización explícita
 * ('AutorizacionAcceso') en estado 'HABILITADO'. El paciente conserva esa potestad desde la
 * app móvil en cualquier momento.
 *
 * SEPARACIÓN DE RESPONSABILIDADES: la verificación de que quien llama es el dueño del
 * registro se hace siempre contra el identificador del token (nunca contra un parámetro
 * de la petición), y la verificación de que la IPS solicitante corresponde al médico
 * autenticado se hace en 'registrarHistoriaClinica' y 'consultarHistoriasDesdeIps'.
 */
@Service
@Transactional
public class HistoriaClinicaService implements HistoriaClinicaUseCase {

    private final HistoriaClinicaRepositoryPort historiaClinicaRepository;
    private final AutorizacionAccesoRepositoryPort autorizacionAccesoRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final IpsRepositoryPort ipsRepository;
    private final MedicoRepositoryPort medicoRepository;
    private final NotificacionPort notificacionPort;

    public HistoriaClinicaService(HistoriaClinicaRepositoryPort historiaClinicaRepository,
                                  AutorizacionAccesoRepositoryPort autorizacionAccesoRepository,
                                  UsuarioRepositoryPort usuarioRepository,
                                  IpsRepositoryPort ipsRepository,
                                  MedicoRepositoryPort medicoRepository,
                                  NotificacionPort notificacionPort) {
        this.historiaClinicaRepository = historiaClinicaRepository;
        this.autorizacionAccesoRepository = autorizacionAccesoRepository;
        this.usuarioRepository = usuarioRepository;
        this.ipsRepository = ipsRepository;
        this.medicoRepository = medicoRepository;
        this.notificacionPort = notificacionPort;
    }

    @Override
    public HistoriaClinica registrarHistoriaClinica(Long medicoUsuarioId, Long pacienteId,
                                                    String diagnostico, String tratamiento, String contenido) {
        Medico medico = medicoRepository.buscarPorUsuarioId(medicoUsuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Registro de médico no encontrado"));

        Usuario paciente = usuarioRepository.buscarPorId(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado"));

        // La custodia del registro la tiene la IPS donde el médico adscrito ejerce.
        Ips ipsOrigen = medico.getIps();

        HistoriaClinica historia = new HistoriaClinica();
        historia.setPaciente(paciente);
        historia.setIpsOrigen(ipsOrigen);
        historia.setDiagnostico(diagnostico);
        historia.setTratamiento(tratamiento);
        historia.setContenido(contenido);
        historia.setFechaRegistro(LocalDateTime.now());

        return historiaClinicaRepository.guardar(historia);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoriaClinica> consultarHistoriasPaciente(Long pacienteId) {
        return historiaClinicaRepository.buscarPorPacienteId(pacienteId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoriaClinica> consultarHistoriasDesdeIps(Long medicoUsuarioId, Long pacienteId, Long ipsSolicitanteId) {
        verificarIpsDelMedico(medicoUsuarioId, ipsSolicitanteId);

        List<HistoriaClinica> todas = historiaClinicaRepository.buscarPorPacienteId(pacienteId);
        List<HistoriaClinica> autorizadas = new ArrayList<>();

        boolean mostroAlgunRegistro = false;
        for (HistoriaClinica hc : todas) {
            // La IPS que generó el registro conserva el acceso directo a su custodia.
            if (hc.getIpsOrigen().getId().equals(ipsSolicitanteId)) {
                autorizadas.add(hc);
                mostroAlgunRegistro = true;
                continue;
            }

            // Cualquier otra IPS necesita autorización explícita y vigente del paciente.
            Optional<AutorizacionAcceso> optAuth = autorizacionAccesoRepository
                    .buscarPorHistoriaEIpsSolicitante(hc.getId(), ipsSolicitanteId);
            if (optAuth.isPresent() && optAuth.get().getEstado() == EstadoAutorizacion.HABILITADO) {
                autorizadas.add(hc);
                mostroAlgunRegistro = true;
            }
        }

        // El paciente sí tiene historia, pero ninguna de ella es accesible a esta IPS:
        // se responde 403 en lugar de una lista vacía, para no simular que no existen.
        // Si el paciente no tiene ningún registro, se devuelve lista vacía sin error.
        if (!mostroAlgunRegistro && !todas.isEmpty()) {
            throw new AccesoDenegadoHistoriaException(
                    "Acceso Denegado por Ley Res. 1995/1999: la IPS solicitante no posee autorización " +
                    "explícita vigente del paciente para consultar registros clínicos de otras instituciones."
            );
        }

        return autorizadas;
    }

    @Override
    public AutorizacionAcceso autorizarAccesoIps(Long historiaId, Long ipsSolicitanteId, Long pacienteId) {
        HistoriaClinica historia = historiaClinicaRepository.buscarPorId(historiaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Historia clínica no encontrada"));

        if (historia.getPaciente() == null || !historia.getPaciente().getId().equals(pacienteId)) {
            throw new AccesoNoAutorizadoException("No posee permisos para otorgar autorizaciones sobre este registro clínico.");
        }

        Ips ipsSolicitante = ipsRepository.buscarPorId(ipsSolicitanteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("IPS solicitante no encontrada"));

        AutorizacionAcceso autorizacion = autorizacionAccesoRepository
                .buscarPorHistoriaEIpsSolicitante(historiaId, ipsSolicitanteId)
                .orElseGet(() -> {
                    AutorizacionAcceso nueva = new AutorizacionAcceso();
                    nueva.setHistoria(historia);
                    nueva.setIpsSolicitante(ipsSolicitante);
                    return nueva;
                });

        autorizacion.setEstado(EstadoAutorizacion.HABILITADO);
        autorizacion.setFechaAutorizacion(LocalDateTime.now());
        autorizacion.setFechaRevocacion(null);

        AutorizacionAcceso guardada = autorizacionAccesoRepository.guardar(autorizacion);

        notificacionPort.enviarNotificacion(
                historia.getPaciente(),
                "Autorizacion de Historia Clinica Actualizada",
                "Has autorizado a " + ipsSolicitante.getNombre() + " para consultar tu registro clínico.",
                TipoNotificacion.PUSH
        );

        return guardada;
    }

    @Override
    public AutorizacionAcceso revocarAccesoIps(Long historiaId, Long ipsSolicitanteId, Long pacienteId) {
        HistoriaClinica historia = historiaClinicaRepository.buscarPorId(historiaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Historia clínica no encontrada"));

        if (historia.getPaciente() == null || !historia.getPaciente().getId().equals(pacienteId)) {
            throw new AccesoNoAutorizadoException("No posee permisos para modificar esta autorización.");
        }

        AutorizacionAcceso autorizacion = autorizacionAccesoRepository
                .buscarPorHistoriaEIpsSolicitante(historiaId, ipsSolicitanteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un registro de autorización previo para esta IPS."));

        autorizacion.setEstado(EstadoAutorizacion.REVOCADO);
        autorizacion.setFechaRevocacion(LocalDateTime.now());

        AutorizacionAcceso guardada = autorizacionAccesoRepository.guardar(autorizacion);

        notificacionPort.enviarNotificacion(
                historia.getPaciente(),
                "Acceso Revocado",
                "Has revocado el permiso de consulta a " + autorizacion.getIpsSolicitante().getNombre() + ".",
                TipoNotificacion.PUSH
        );

        return guardada;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AutorizacionAcceso> obtenerAutorizacionesPaciente(Long pacienteId) {
        return autorizacionAccesoRepository.buscarPorPacienteId(pacienteId);
    }

    /**
     * Verifica que la IPS solicitante corresponda a la del médico autenticado, para que un
     * profesional no pueda hacerse pasar por otra IPS al solicitar historiales.
     */
    private void verificarIpsDelMedico(Long medicoUsuarioId, Long ipsSolicitanteId) {
        Medico medico = medicoRepository.buscarPorUsuarioId(medicoUsuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Registro de médico no encontrado"));
        if (!medico.getIps().getId().equals(ipsSolicitanteId)) {
            throw new AccesoNoAutorizadoException("La IPS solicitante no corresponde a su adscripción.");
        }
    }
}
