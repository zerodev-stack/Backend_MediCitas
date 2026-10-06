package co.edu.monteria.medicitas.application.service;

import co.edu.monteria.medicitas.application.ports.in.ReasignacionFifoUseCase;
import co.edu.monteria.medicitas.application.ports.out.CitaRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.ColaEsperaRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.DisponibilidadRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.NotificacionPort;
import co.edu.monteria.medicitas.domain.model.Cita;
import co.edu.monteria.medicitas.domain.model.ColaEspera;
import co.edu.monteria.medicitas.domain.model.DisponibilidadMedico;
import co.edu.monteria.medicitas.domain.model.Usuario;
import co.edu.monteria.medicitas.domain.model.enums.EstadoCita;
import co.edu.monteria.medicitas.domain.model.enums.EstadoColaEspera;
import co.edu.monteria.medicitas.domain.model.enums.EstadoDisponibilidad;
import co.edu.monteria.medicitas.domain.model.enums.TipoCita;
import co.edu.monteria.medicitas.domain.model.enums.TipoNotificacion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio del caso de uso transversal de REASIGNACIÓN AUTOMÁTICA FIFO.
 *
 * POR QUÉ Y CÓMO DE LA REASIGNACIÓN AUTOMÁTICA (COLA FIFO):
 * En los sistemas de agendamiento en salud, la inasistencia o la cancelación imprevista
 * generan huecos de disponibilidad desperdiciados. MediCitas los recupera de forma
 * determinista: la solicitud más antigua gana.
 *
 * Cuando un cupo queda libre, este servicio:
 * 1. Toma la CABECERA de la cola (la solicitud EN_ESPERA más antigua) con bloqueo de
 *    escritura, de modo que dos liberaciones simultáneas atiendan a dos personas distintas.
 * 2. Pasa el cupo de LIBRE a OCUPADO con una transición condicionada, que vuelve a fallar
 *    si otra transacción se adelantó.
 * 3. Crea la cita a nombre del paciente en cola y marca su solicitud como ASIGNADO.
 * 4. Envía la notificación inmediata.
 *
 * Si el cupo fue tomado por otro antes de llegar al paso 2, la transacción se revierte
 * completa y la solicitud permanece EN_ESPERA para el siguiente cupo libre. Ese detalle es
 * lo que garantiza que ninguna cola quede marcada como atendida sin cita real.
 */
@Service
@Transactional
public class ReasignacionFifoService implements ReasignacionFifoUseCase {

    private final ColaEsperaRepositoryPort colaEsperaRepository;
    private final DisponibilidadRepositoryPort disponibilidadRepository;
    private final CitaRepositoryPort citaRepository;
    private final NotificacionPort notificacionPort;

    public ReasignacionFifoService(ColaEsperaRepositoryPort colaEsperaRepository,
                                   DisponibilidadRepositoryPort disponibilidadRepository,
                                   CitaRepositoryPort citaRepository,
                                   NotificacionPort notificacionPort) {
        this.colaEsperaRepository = colaEsperaRepository;
        this.disponibilidadRepository = disponibilidadRepository;
        this.citaRepository = citaRepository;
        this.notificacionPort = notificacionPort;
    }

    @Override
    public boolean reasignarSiHayEspera(DisponibilidadMedico disponibilidad) {
        if (disponibilidad == null || disponibilidad.getId() == null) {
            return false;
        }
        if (disponibilidad.getMedico() == null
                || disponibilidad.getMedico().getEspecialidad() == null
                || disponibilidad.getMedico().getIps() == null) {
            return false;
        }

        Long especialidadId = disponibilidad.getMedico().getEspecialidad().getId();
        Long ipsId = disponibilidad.getMedico().getIps().getId();

        // (1) Cabecera de la cola con bloqueo de escritura: es el recurso disputado.
        Optional<ColaEspera> optCola = colaEsperaRepository.obtenerCabeceraFifoBloqueada(
                especialidadId, ipsId, EstadoColaEspera.EN_ESPERA);

        if (optCola.isEmpty()) {
            return false;
        }
        ColaEspera primerEnEspera = optCola.get();

        // (2) Transición condicionada del cupo. Si otra transacción ya lo tomó, se aborta
        //     toda la operación y la solicitud sigue en cola para el próximo cupo.
        boolean cupoTomado = disponibilidadRepository.transicionarEstadoSiCoincide(
                disponibilidad.getId(), EstadoDisponibilidad.LIBRE, EstadoDisponibilidad.OCUPADO);

        if (!cupoTomado) {
            return false;
        }
        disponibilidad.setEstado(EstadoDisponibilidad.OCUPADO);

        // (3) Cita automática a nombre del paciente que heads la cola.
        Cita citaAutomatica = new Cita();
        citaAutomatica.setPaciente(primerEnEspera.getPaciente());
        citaAutomatica.setDisponibilidad(disponibilidad);
        citaAutomatica.setIps(disponibilidad.getMedico().getIps());
        citaAutomatica.setTipo(TipoCita.CONSULTA);
        citaAutomatica.setEstado(EstadoCita.PROGRAMADA);
        citaAutomatica.setObservaciones("Asignada automáticamente por la Cola de Espera FIFO de MediCitas");
        citaAutomatica.setFechaCreacion(LocalDateTime.now());
        citaRepository.guardar(citaAutomatica);

        primerEnEspera.setEstado(EstadoColaEspera.ASIGNADO);
        colaEsperaRepository.guardar(primerEnEspera);

        // (4) Notificación inmediata al beneficiario.
        Usuario paciente = primerEnEspera.getPaciente();
        notificacionPort.enviarNotificacion(
                paciente,
                "Cupo Liberado Asignado Automaticamente",
                "Se ha liberado un cupo de " + disponibilidad.getMedico().getEspecialidad().getNombre() +
                " en " + disponibilidad.getMedico().getIps().getNombre() +
                " para el " + disponibilidad.getFecha() + " a las " + disponibilidad.getHoraInicio() +
                ". Ha sido agendado automaticamente a tu nombre por la Cola de Espera de MediCitas.",
                TipoNotificacion.PUSH
        );

        return true;
    }
}
