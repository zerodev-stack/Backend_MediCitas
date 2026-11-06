package co.edu.monteria.medicitas.application.service;

import co.edu.monteria.medicitas.application.ports.in.GestionarColaMedicoUseCase;
import co.edu.monteria.medicitas.application.ports.out.CitaRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.ColaEsperaRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.DisponibilidadRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.MedicoRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.NotificacionPort;
import co.edu.monteria.medicitas.domain.exception.AccesoNoAutorizadoException;
import co.edu.monteria.medicitas.domain.exception.ConflictoConcurrenciaException;
import co.edu.monteria.medicitas.domain.exception.RecursoNoEncontradoException;
import co.edu.monteria.medicitas.domain.exception.ReglaNegocioException;
import co.edu.monteria.medicitas.domain.model.Cita;
import co.edu.monteria.medicitas.domain.model.ColaEspera;
import co.edu.monteria.medicitas.domain.model.DisponibilidadMedico;
import co.edu.monteria.medicitas.domain.model.Medico;
import co.edu.monteria.medicitas.domain.model.enums.EstadoBloque;
import co.edu.monteria.medicitas.domain.model.enums.EstadoCita;
import co.edu.monteria.medicitas.domain.model.enums.EstadoColaEspera;
import co.edu.monteria.medicitas.domain.model.enums.EstadoDisponibilidad;
import co.edu.monteria.medicitas.domain.model.enums.TipoCita;
import co.edu.monteria.medicitas.domain.model.enums.TipoNotificacion;
import co.edu.monteria.medicitas.domain.model.enums.TipoServicio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Cola de espera desde el lado del médico.
 *
 * La reasignación automática FIFO sigue existiendo y actúa sola cuando se libera o se
 * publica un cupo. Este servicio cubre el caso que la automática no resuelve: el médico ve
 * quién espera y decide darle un cupo concreto suyo. Una solicitud solo es visible para los
 * médicos de su misma especialidad e IPS.
 */
@Service
@Transactional
public class ColaMedicoService implements GestionarColaMedicoUseCase {

    private final MedicoRepositoryPort medicoRepository;
    private final ColaEsperaRepositoryPort colaEsperaRepository;
    private final DisponibilidadRepositoryPort disponibilidadRepository;
    private final CitaRepositoryPort citaRepository;
    private final NotificacionPort notificacionPort;

    public ColaMedicoService(MedicoRepositoryPort medicoRepository,
                             ColaEsperaRepositoryPort colaEsperaRepository,
                             DisponibilidadRepositoryPort disponibilidadRepository,
                             CitaRepositoryPort citaRepository,
                             NotificacionPort notificacionPort) {
        this.medicoRepository = medicoRepository;
        this.colaEsperaRepository = colaEsperaRepository;
        this.disponibilidadRepository = disponibilidadRepository;
        this.citaRepository = citaRepository;
        this.notificacionPort = notificacionPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ColaEspera> obtenerColaDelMedico(Long medicoUsuarioId) {
        Medico medico = medico(medicoUsuarioId);
        return colaEsperaRepository.buscarEnEspera(medico.getEspecialidad().getId(), medico.getIps().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public ColaEspera obtenerSolicitud(Long medicoUsuarioId, Long colaId) {
        return solicitudVisible(medico(medicoUsuarioId), colaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisponibilidadMedico> cuposCompatibles(Long medicoUsuarioId, Long colaId) {
        Medico medico = medico(medicoUsuarioId);
        ColaEspera solicitud = solicitudVisible(medico, colaId);
        return disponibilidadRepository.buscarLibresDeMedicoDesde(medico.getId(), LocalDate.now()).stream()
                .filter(ColaMedicoService::esFuturo)
                .filter(ColaMedicoService::bloqueVigente)
                .filter(cupo -> esCompatible(solicitud, cupo))
                .toList();
    }

    @Override
    public Cita asignarCupo(Long medicoUsuarioId, Long colaId, Long disponibilidadId) {
        Medico medico = medico(medicoUsuarioId);
        ColaEspera solicitud = solicitudVisible(medico, colaId);
        if (solicitud.getEstado() != EstadoColaEspera.EN_ESPERA) {
            throw new ReglaNegocioException("Esta solicitud ya salió de la cola de espera.");
        }

        DisponibilidadMedico cupo = disponibilidadRepository.buscarPorId(disponibilidadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cupo no encontrado"));
        if (!cupo.getMedico().getId().equals(medico.getId())) {
            throw new AccesoNoAutorizadoException("Solo puedes asignar cupos de tu propia agenda.");
        }
        if (!esCompatible(solicitud, cupo)) {
            throw new ReglaNegocioException("El cupo no corresponde al servicio o la sede que pidió el paciente.");
        }
        if (cupo.getEstado() != EstadoDisponibilidad.LIBRE || !esFuturo(cupo) || !bloqueVigente(cupo)) {
            throw new ReglaNegocioException("El cupo seleccionado ya no está disponible.");
        }

        // Misma reserva atómica que usa el paciente al agendar: si otro paciente tomó el
        // cupo entre la lectura y este punto, se informa como conflicto (HTTP 409).
        boolean reservado = disponibilidadRepository.transicionarEstadoSiCoincide(
                disponibilidadId, EstadoDisponibilidad.LIBRE, EstadoDisponibilidad.OCUPADO);
        if (!reservado) {
            throw new ConflictoConcurrenciaException("Otro paciente acaba de reservar este cupo. Elige otro.");
        }
        cupo.setEstado(EstadoDisponibilidad.OCUPADO);

        Cita cita = new Cita();
        cita.setPaciente(solicitud.getPaciente());
        cita.setDisponibilidad(cupo);
        cita.setIps(medico.getIps());
        cita.setTipo(cupo.getBloque() != null && cupo.getBloque().getServicio().getTipo() == TipoServicio.EXAMEN
                ? TipoCita.EXAMEN : TipoCita.CONSULTA);
        cita.setEstado(EstadoCita.PROGRAMADA);
        cita.setObservaciones("Asignada por el profesional desde la cola de espera");
        cita.setFechaCreacion(LocalDateTime.now());
        Cita guardada = citaRepository.guardar(cita);

        solicitud.setEstado(EstadoColaEspera.ASIGNADO);
        colaEsperaRepository.guardar(solicitud);

        String servicio = cupo.getBloque() != null
                ? cupo.getBloque().getServicio().getNombre()
                : medico.getEspecialidad().getNombre();
        String lugar = medico.getIps().getNombre()
                + (cupo.getBloque() != null ? " · " + cupo.getBloque().getSede().getNombre() : "");
        notificacionPort.enviarNotificacion(
                solicitud.getPaciente(),
                "Tu médico te asignó un cupo",
                "Saliste de la cola de espera: tienes cita de " + servicio + " con " + medico.getUsuario().getNombreCompleto()
                        + " el " + cupo.getFecha() + " a las " + cupo.getHoraInicio() + " en " + lugar + ".",
                TipoNotificacion.PUSH
        );
        return guardada;
    }

    /** La solicitud debe existir y pertenecer a la especialidad e IPS del médico. */
    private ColaEspera solicitudVisible(Medico medico, Long colaId) {
        ColaEspera solicitud = colaEsperaRepository.buscarPorId(colaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud de cola de espera no encontrada"));
        boolean misma = solicitud.getEspecialidad().getId().equals(medico.getEspecialidad().getId())
                && solicitud.getIps().getId().equals(medico.getIps().getId());
        if (!misma) {
            throw new AccesoNoAutorizadoException("Esta solicitud corresponde a otra especialidad o institución.");
        }
        return solicitud;
    }

    private static boolean esFuturo(DisponibilidadMedico cupo) {
        LocalDate hoy = LocalDate.now();
        return cupo.getFecha().isAfter(hoy)
                || (cupo.getFecha().equals(hoy) && cupo.getHoraInicio().isAfter(LocalTime.now()));
    }

    /**
     * Si la solicitud pidió servicio o sede, el cupo debe venir de un bloque con esos mismos
     * datos. Una solicitud sin preferencias acepta cualquier cupo del médico.
     */
    private static boolean esCompatible(ColaEspera solicitud, DisponibilidadMedico cupo) {
        var bloque = cupo.getBloque();
        if (solicitud.getServicio() != null
                && (bloque == null || !bloque.getServicio().getId().equals(solicitud.getServicio().getId()))) {
            return false;
        }
        return solicitud.getSede() == null
                || (bloque != null && bloque.getSede().getId().equals(solicitud.getSede().getId()));
    }

    private static boolean bloqueVigente(DisponibilidadMedico cupo) {
        return cupo.getBloque() == null || cupo.getBloque().getEstado() == EstadoBloque.ACTIVO;
    }

    private Medico medico(Long medicoUsuarioId) {
        return medicoRepository.buscarPorUsuarioId(medicoUsuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Registro de médico no encontrado"));
    }
}
