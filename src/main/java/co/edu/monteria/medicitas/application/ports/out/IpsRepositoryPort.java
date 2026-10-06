package co.edu.monteria.medicitas.application.ports.out;

import co.edu.monteria.medicitas.domain.model.Ips;
import java.util.List;
import java.util.Optional;

public interface IpsRepositoryPort {
    List<Ips> obtenerTodas();
    Optional<Ips> buscarPorId(Long id);
    List<Ips> obtenerPorEpsId(Long epsId);
}
