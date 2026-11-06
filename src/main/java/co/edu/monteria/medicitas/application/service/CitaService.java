package co.edu.monteria.medicitas.application.service;

import co.edu.monteria.medicitas.application.ports.in.AgendarCitaUseCase;
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
import co.edu.monteria.medicitas.domain.model.Usuario;
import co.edu.monteria.medicitas.domain.model.enums.EstadoCita;
import co.edu.monteria.medicitas.domain.model.enums.EstadoDisponibilidad;
import co.edu.monteria.medicitas.domain.model.enums.TipoCita;
import co.edu.monteria.medicitas.domain.model.enums.TipoNotificacion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de Aplicación para el Agendamiento y Gestión de Citas Médicas.
 *
 * TODA operación recibe el identificador del paciente como parámetro y verifica que la
 * cita pertenezca a ese paciente. La capa web obtiene ese identificador del token JWT, de
 * modo que un usuario no puede operar sobre citas ajenas ni declararse como otro
 * paciente cambiando un parámetro de la petición.
 */
@Service
@Transactional
public class CitaService implements AgendarCitaUseCase {

    private final CitaRepositoryPort citaRepository;
    private final DisponibilidadRepositoryPort disponibilidadRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final MedicoRepositoryPort medicoRepository;
    private final NotificacionPort notificacionPort;
    private final ReasignacionFifoUseCase reasignacionFifoUseCase;

    public CitaService(CitaRepositoryPort citaRepository,
                       DisponibilidadRepositoryPort disponibilidadRepository,
                       UsuarioRepositoryPort usuarioRepository,
                       MedicoRepositoryPort medicoRepository,
                       NotificacionPort notificacionPort,
                       ReasignacionFifoUseCase reasignacionFifoUseCase) {
        this.citaRepository = citaRepository;
        this.disponibilidadRepository = disponibilidadRepository;
        this.usuarioRepository = usuarioRepository;
        this.medicoRepository = medicoRepository;
        this.notificacionPort = notificacionPort;
        this.reasignacionFifoUseCase = reasignacionFifoUseCase;
    }

    @Override
    public Cita agendarCita(Long pacienteId, Long disponibilidadId, TipoCita tipo, String observaciones) {
        Usuario paciente = usuarioRepository.buscarPorId(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado"));

        DisponibilidadMedico disponibilidad = disponibilidadRepository.buscarPorId(disponibilidadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Franja horaria no encontrada"));

        if (disponibilidad.getEstado() != EstadoDisponibilidad.LIBRE) {
            throw new ReglaNegocioException("La franja horaria seleccionada ya no se encuentra disponible.");
        }

        // Reserva atómica del cupo: si otra petición ganó la carrera entre la lectura y
        // este punto, la transición no se aplica y se informa como conflicto (HTTP 409).
        boolean reservado = disponibilidadRepository.transicionarEstadoSiCoincide(
                disponibilidadId, EstadoDisponibilidad.LIBRE, EstadoDisponibilidad.OCUPADO);

        if (!reservado) {
            throw new ConflictoConcurrenciaException(
                    "Otro paciente reservó este cupo segundos antes. Por favor elija otra franja.");
        }
        disponibilidad.setEstado(EstadoDisponibilidad.OCUPADO);

        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setDisponibilidad(disponibilidad);
        cita.setIps(disponibilidad.getMedico().getIps());
        cita.setTipo(tipo != null ? tipo : TipoCita.CONSULTA);
        cita.setEstado(EstadoCita.PROGRAMADA);
        cita.setObservaciones(observaciones);
        cita.setFechaCreacion(LocalDateTime.now());

        Cita citaGuardada = citaRepository.guardar(cita);

        notificacionPort.enviarNotificacion(
                paciente,
                "Cita Agendada Exitosamente",
                "Tu cita de " + disponibilidad.getMedico().getEspecialidad().getNombre() +
                " en " + disponibilidad.getMedico().getIps().getNombre() +
                " ha sido confirmada para el " + disponibilidad.getFecha() + " a las " + disponibilidad.getHoraInicio(),
                TipoNotificacion.PUSH
        );

        return citaGuardada;
    }

    @Override
    public Cita cancelarCita(Long citaId, Long usuarioId) {
        Cita cita = citaRepository.buscarPorId(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita médica no encontrada"));

        verificarPertenencia(cita, usuarioId, "cancelar");

        if (cita.getEstado() != EstadoCita.PROGRAMADA) {
            throw new ReglaNegocioException("Solo es posible cancelar citas en estado PROGRAMADA.");
        }

        cita.setEstado(EstadoCita.CANCELADA);
        citaRepository.guardar(cita);

        // Liberar el cupo. La transición es condicionada a OCUPADO -> LIBRE.
        DisponibilidadMedico disponibilidad = cita.getDisponibilidad();
        if (disponibilidad != null) {
            disponibilidadRepository.transicionarEstadoSiCoincide(
                    disponibilidad.getId(), EstadoDisponibilidad.OCUPADO, EstadoDisponibilidad.LIBRE);
            disponibilidad.setEstado(EstadoDisponibilidad.LIBRE);
        }

        notificacionPort.enviarNotificacion(
                cita.getPaciente(),
                "Cita Cancelada",
                "Has cancelado tu cita de " + disponibilidad.getMedico().getEspecialidad().getNombre() +
                " para el " + disponibilidad.getFecha() + ". El cupo vuelve a estar disponible.",
                TipoNotificacion.PUSH
        );

        // El cupo liberado puede atender a la primera persona de la cola FIFO.
        reasignacionFifoUseCase.reasignarSiHayEspera(disponibilidad);

        return cita;
    }

    @Override
    public Cita aplazarCita(Long citaId, Long nuevaDisponibilidadId, Long pacienteId) {
        Cita citaOriginal = citaRepository.buscarPorId(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita original no encontrada"));

        verificarPertenencia(citaOriginal, pacienteId, "aplazar");

        if (citaOriginal.getEstado() != EstadoCita.PROGRAMADA) {
            throw new ReglaNegocioException("Solo es posible aplazar citas en estado PROGRAMADA.");
        }

        DisponibilidadMedico nuevaDisponibilidad = disponibilidadRepository.buscarPorId(nuevaDisponibilidadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Nueva disponibilidad no encontrada"));

        // El orden de estas comprobaciones es intencionado: primero se descarta la
        // franja que la propia cita ya ocupa, porque su estado es OCUPADO y el paciente
        // recibiría un mensaje engañoso si se comprobara la disponibilidad antes.
        if (nuevaDisponibilidadId.equals(citaOriginal.getDisponibilidad().getId())) {
            throw new ReglaNegocioException("La nueva franja debe ser distinta de la actual.");
        }
        if (nuevaDisponibilidad.getEstado() != EstadoDisponibilidad.LIBRE) {
            throw new ReglaNegocioException("El nuevo horario seleccionado ya no está disponible.");
        }
        if (!mismaEspecialidadEIps(citaOriginal.getDisponibilidad(), nuevaDisponibilidad)) {
            throw new ReglaNegocioException(
                    "La nueva franja pertenece a otra especialidad o IPS. El aplazamiento solo admite " +
                    "horarios de la misma especialidad e IPS de la cita original.");
        }

        boolean reservada = disponibilidadRepository.transicionarEstadoSiCoincide(
                nuevaDisponibilidadId, EstadoDisponibilidad.LIBRE, EstadoDisponibilidad.OCUPADO);
        if (!reservada) {
            throw new ConflictoConcurrenciaException("Ese horario acaba de ser tomado por otro paciente.");
        }
        nuevaDisponibilidad.setEstado(EstadoDisponibilidad.OCUPADO);

        // Liberar la disponibilidad previa y pasar la cita a APLAZADA.
        DisponibilidadMedico dispPrevia = citaOriginal.getDisponibilidad();
        disponibilidadRepository.transicionarEstadoSiCoincide(
                dispPrevia.getId(), EstadoDisponibilidad.OCUPADO, EstadoDisponibilidad.LIBRE);
        dispPrevia.setEstado(EstadoDisponibilidad.LIBRE);

        citaOriginal.setEstado(EstadoCita.APLAZADA);
        citaRepository.guardar(citaOriginal);

        Cita nuevaCita = new Cita();
        nuevaCita.setPaciente(citaOriginal.getPaciente());
        nuevaCita.setDisponibilidad(nuevaDisponibilidad);
        nuevaCita.setIps(nuevaDisponibilidad.getMedico().getIps());
        nuevaCita.setTipo(citaOriginal.getTipo());
        nuevaCita.setEstado(EstadoCita.PROGRAMADA);
        nuevaCita.setObservaciones("Reagendada a partir de la cita #" + citaId);
        nuevaCita.setFechaCreacion(LocalDateTime.now());

        Cita guardada = citaRepository.guardar(nuevaCita);

        // El hueco que quedó libre puede atender a la cola FIFO.
        reasignacionFifoUseCase.reasignarSiHayEspera(dispPrevia);

        return guardada;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> obtenerCitasPaciente(Long pacienteId) {
        return citaRepository.buscarPorPacienteId(pacienteId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> obtenerCitasMedico(Long usuarioMedicoId) {
        Long medicoId = medicoRepository.buscarPorUsuarioId(usuarioMedicoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Registro de médico no encontrado"))
                .getId();
        return citaRepository.buscarPorMedicoId(medicoId);
    }

    @Override
    public Cita marcarAtendida(Long citaId, Long medicoUsuarioId) {
        Cita cita = citaDeMiAgenda(citaId, medicoUsuarioId);
        if (cita.getEstado() != EstadoCita.PROGRAMADA) {
            throw new ReglaNegocioException("Solo es posible marcar como atendida una cita PROGRAMADA.");
        }
        if (cita.getDisponibilidad().getFecha().isAfter(LocalDate.now())) {
            throw new ReglaNegocioException("Solo puedes marcar como atendida una cita de hoy o de días anteriores.");
        }
        cita.setEstado(EstadoCita.ATENDIDA);
        return citaRepository.guardar(cita);
    }

    @Override
    public Cita cancelarPorMedico(Long citaId, Long medicoUsuarioId) {
        Cita cita = citaDeMiAgenda(citaId, medicoUsuarioId);
        if (cita.getEstado() != EstadoCita.PROGRAMADA) {
            throw new ReglaNegocioException("Solo es posible cancelar citas en estado PROGRAMADA.");
        }
        cita.setEstado(EstadoCita.CANCELADA);
        citaRepository.guardar(cita);

        // A diferencia de la cancelación del paciente, el cupo NO vuelve a quedar libre:
        // el médico no puede atender en ese horario, así que ofrecerlo a otro paciente o a
        // la cola FIFO generaría una cita que tampoco se cumpliría.
        DisponibilidadMedico disponibilidad = cita.getDisponibilidad();
        disponibilidadRepository.transicionarEstadoSiCoincide(
                disponibilidad.getId(), EstadoDisponibilidad.OCUPADO, EstadoDisponibilidad.CANCELADO);
        disponibilidad.setEstado(EstadoDisponibilidad.CANCELADO);

        notificacionPort.enviarNotificacion(
                cita.getPaciente(),
                "Tu cita fue cancelada por el profesional",
                disponibilidad.getMedico().getUsuario().getNombreCompleto() + " canceló tu cita de "
                        + disponibilidad.getMedico().getEspecialidad().getNombre() + " del " + disponibilidad.getFecha()
                        + " a las " + disponibilidad.getHoraInicio() + ". Puedes agendar un nuevo horario desde la app.",
                TipoNotificacion.PUSH
        );
        return cita;
    }

    private Cita citaDeMiAgenda(Long citaId, Long medicoUsuarioId) {
        Cita cita = citaRepository.buscarPorId(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita médica no encontrada"));
        Long duenoAgenda = cita.getDisponibilidad().getMedico().getUsuario().getId();
        if (!duenoAgenda.equals(medicoUsuarioId)) {
            throw new AccesoNoAutorizadoException("Esta cita no pertenece a tu agenda.");
        }
        return cita;
    }

    private void verificarPertenencia(Cita cita, Long pacienteId, String operacion) {
        if (cita.getPaciente() == null || !cita.getPaciente().getId().equals(pacienteId)) {
            throw new AccesoNoAutorizadoException("No está autorizado para " + operacion + " esta cita médica.");
        }
    }

    private boolean mismaEspecialidadEIps(DisponibilidadMedico a, DisponibilidadMedico b) {
        return a.getMedico().getEspecialidad().getId().equals(b.getMedico().getEspecialidad().getId())
                && a.getMedico().getIps().getId().equals(b.getMedico().getIps().getId());
    }
}
