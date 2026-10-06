package co.edu.monteria.medicitas.application.ports.in;

import co.edu.monteria.medicitas.domain.model.DisponibilidadMedico;

/**
 * Caso de uso transversal: asigna automáticamente a la cola de espera FIFO el cupo liberado
 * por una cancelación, un aplazamiento o la publicación de una nueva franja horaria.
 *
 * POR QUÉ ES UN CASO DE USO APARTE: tanto 'AgendarCitaUseCase' como
 * 'GestionarDisponibilidadUseCase' necesitan disparar este comportamiento, pero ninguno
 * debe depender del otro. Extraerlo a un puerto propio mantiene la capa de aplicación libre
 * de acoplamiento entre servicios y garantiza que la reasignación tenga una única
 * implementación (incluido su control de concurrencia).
 */
public interface ReasignacionFifoUseCase {

    /**
     * Intenta asignar el cupo a la primera solicitud en espera FIFO que coincida con la
     * especialidad e IPS de la disponibilidad. Si no hay nadie en cola, no hace nada.
     *
     * @return true si se asignó el cupo a un paciente de la cola.
     */
    boolean reasignarSiHayEspera(DisponibilidadMedico disponibilidad);
}
