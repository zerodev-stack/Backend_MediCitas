package co.edu.monteria.medicitas.application.ports.in;

import co.edu.monteria.medicitas.domain.model.Eps;
import co.edu.monteria.medicitas.domain.model.Especialidad;
import co.edu.monteria.medicitas.domain.model.Ips;

import java.util.List;

public interface ConsultarEntidadesUseCase {
    List<Eps> obtenerEps();
    List<Ips> obtenerIps();
    List<Ips> obtenerIpsPorEps(Long epsId);
    List<Especialidad> obtenerEspecialidades();
}
