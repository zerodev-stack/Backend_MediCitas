package co.edu.monteria.medicitas.application.service;

import co.edu.monteria.medicitas.application.ports.in.ConsultarEntidadesUseCase;
import co.edu.monteria.medicitas.application.ports.out.EpsRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.EspecialidadRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.IpsRepositoryPort;
import co.edu.monteria.medicitas.domain.model.Eps;
import co.edu.monteria.medicitas.domain.model.Especialidad;
import co.edu.monteria.medicitas.domain.model.Ips;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EntidadService implements ConsultarEntidadesUseCase {

    private final EpsRepositoryPort epsRepository;
    private final IpsRepositoryPort ipsRepository;
    private final EspecialidadRepositoryPort especialidadRepository;

    public EntidadService(EpsRepositoryPort epsRepository, IpsRepositoryPort ipsRepository, EspecialidadRepositoryPort especialidadRepository) {
        this.epsRepository = epsRepository;
        this.ipsRepository = ipsRepository;
        this.especialidadRepository = especialidadRepository;
    }

    @Override
    public List<Eps> obtenerEps() {
        return epsRepository.obtenerTodas();
    }

    @Override
    public List<Ips> obtenerIps() {
        return ipsRepository.obtenerTodas();
    }

    @Override
    public List<Ips> obtenerIpsPorEps(Long epsId) {
        return ipsRepository.obtenerPorEpsId(epsId);
    }

    @Override
    public List<Especialidad> obtenerEspecialidades() {
        return especialidadRepository.obtenerTodas();
    }
}
