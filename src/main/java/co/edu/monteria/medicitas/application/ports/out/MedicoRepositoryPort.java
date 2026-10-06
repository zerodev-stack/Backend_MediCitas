package co.edu.monteria.medicitas.application.ports.out;

import co.edu.monteria.medicitas.domain.model.Medico;
import java.util.Optional;

public interface MedicoRepositoryPort {
    Medico guardar(Medico medico);
    Optional<Medico> buscarPorId(Long id);
    Optional<Medico> buscarPorUsuarioId(Long usuarioId);
}
