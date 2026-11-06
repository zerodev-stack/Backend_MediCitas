package co.edu.monteria.medicitas.application.ports.out;

import co.edu.monteria.medicitas.domain.model.Sede;

import java.util.List;
import java.util.Optional;

public interface SedeRepositoryPort {
    Optional<Sede> buscarPorId(Long id);
    List<Sede> buscarPorIpsId(Long ipsId);
}
