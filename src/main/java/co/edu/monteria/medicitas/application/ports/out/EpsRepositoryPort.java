package co.edu.monteria.medicitas.application.ports.out;

import co.edu.monteria.medicitas.domain.model.Eps;
import java.util.List;
import java.util.Optional;

public interface EpsRepositoryPort {
    List<Eps> obtenerTodas();
    Optional<Eps> buscarPorId(Long id);
    Optional<Eps> buscarPorCodigoBdua(String codigoBdua);
}
