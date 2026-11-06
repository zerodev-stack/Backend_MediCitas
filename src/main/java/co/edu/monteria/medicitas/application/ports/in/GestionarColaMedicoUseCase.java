package co.edu.monteria.medicitas.application.ports.in;

import co.edu.monteria.medicitas.domain.model.Cita;
import co.edu.monteria.medicitas.domain.model.ColaEspera;
import co.edu.monteria.medicitas.domain.model.DisponibilidadMedico;

import java.util.List;

/**
 * Cola de espera vista desde el médico: los pacientes que esperan cupo en su especialidad
 * e IPS, y la asignación manual de uno de sus cupos libres.
 */
public interface GestionarColaMedicoUseCase {

    /** Solicitudes EN_ESPERA de la especialidad e IPS del médico, en orden FIFO. */
    List<ColaEspera> obtenerColaDelMedico(Long medicoUsuarioId);

    ColaEspera obtenerSolicitud(Long medicoUsuarioId, Long colaId);

    /** Cupos libres y futuros del propio médico que pueden asignarse a la solicitud. */
    List<DisponibilidadMedico> cuposCompatibles(Long medicoUsuarioId, Long colaId);

    /**
     * Registra la cita a nombre del paciente en el cupo elegido, saca la solicitud de la
     * cola y notifica al paciente.
     */
    Cita asignarCupo(Long medicoUsuarioId, Long colaId, Long disponibilidadId);
}
