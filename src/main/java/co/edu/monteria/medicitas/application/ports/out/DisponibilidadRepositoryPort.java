package co.edu.monteria.medicitas.application.ports.out;

import co.edu.monteria.medicitas.domain.model.DisponibilidadMedico;
import co.edu.monteria.medicitas.domain.model.enums.EstadoDisponibilidad;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DisponibilidadRepositoryPort {
    DisponibilidadMedico guardar(DisponibilidadMedico disponibilidad);
    Optional<DisponibilidadMedico> buscarPorId(Long id);
    List<DisponibilidadMedico> buscarDisponibles(Long especialidadId, Long ipsId, LocalDate fecha);
    List<DisponibilidadMedico> buscarPorMedicoId(Long medicoId);

    /**
     * Cambia el estado del cupo sólo si se encuentra en el estado esperado.
     *
     * @return true si la transición se aplicó; false si el cupo ya no estaba en ese estado,
     *         lo que significa que otra transacción se adelantó.
     */
    boolean transicionarEstadoSiCoincide(Long disponibilidadId,
                                         EstadoDisponibilidad esperado,
                                         EstadoDisponibilidad nuevo);
}
