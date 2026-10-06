package co.edu.monteria.medicitas.application.ports.out;

import co.edu.monteria.medicitas.domain.model.HistoriaClinica;
import java.util.List;
import java.util.Optional;

public interface HistoriaClinicaRepositoryPort {
    HistoriaClinica guardar(HistoriaClinica historia);
    Optional<HistoriaClinica> buscarPorId(Long id);
    List<HistoriaClinica> buscarPorPacienteId(Long pacienteId);
}
