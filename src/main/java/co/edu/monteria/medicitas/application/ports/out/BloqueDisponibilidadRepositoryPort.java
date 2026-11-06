package co.edu.monteria.medicitas.application.ports.out;

import co.edu.monteria.medicitas.domain.model.BloqueDisponibilidad;
import co.edu.monteria.medicitas.domain.model.enums.EstadoBloque;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BloqueDisponibilidadRepositoryPort {
    BloqueDisponibilidad guardar(BloqueDisponibilidad bloque);
    Optional<BloqueDisponibilidad> buscarPorId(Long id);
    List<BloqueDisponibilidad> buscarPorMedicoYEstado(Long medicoId, EstadoBloque estado);
    List<BloqueDisponibilidad> buscarPorMedicoFechaYEstado(Long medicoId, LocalDate fecha, EstadoBloque estado);
}
