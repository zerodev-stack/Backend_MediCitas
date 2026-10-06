package co.edu.monteria.medicitas.application.ports.out;

import co.edu.monteria.medicitas.domain.model.Cita;
import java.util.List;
import java.util.Optional;

public interface CitaRepositoryPort {
    Cita guardar(Cita cita);
    Optional<Cita> buscarPorId(Long id);
    List<Cita> buscarPorPacienteId(Long pacienteId);
    List<Cita> buscarPorMedicoId(Long medicoId);
}
