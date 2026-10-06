package co.edu.monteria.medicitas.application.ports.out;

import co.edu.monteria.medicitas.domain.model.Especialidad;
import java.util.List;
import java.util.Optional;

public interface EspecialidadRepositoryPort {
    List<Especialidad> obtenerTodas();
    Optional<Especialidad> buscarPorId(Long id);
}
