package co.edu.monteria.medicitas.application.ports.out;

import co.edu.monteria.medicitas.domain.model.Servicio;

import java.util.List;
import java.util.Optional;

public interface ServicioRepositoryPort {
    Optional<Servicio> buscarPorId(Long id);
    List<Servicio> buscarPorEspecialidadId(Long especialidadId);
}
