package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.adapter;

import co.edu.monteria.medicitas.application.ports.out.IpsRepositoryPort;
import co.edu.monteria.medicitas.domain.model.Ips;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.mapper.DomainEntityMapper;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository.IpsJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
public class IpsPersistenceAdapter implements IpsRepositoryPort {

    private final IpsJpaRepository repository;
    private final DomainEntityMapper mapper;

    public IpsPersistenceAdapter(IpsJpaRepository repository, DomainEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<Ips> obtenerTodas() {
        return repository.findAll().stream().map(mapper::toIpsDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Ips> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::toIpsDomain);
    }

    @Override
    public List<Ips> obtenerPorEpsId(Long epsId) {
        return repository.findByEpsId(epsId).stream().map(mapper::toIpsDomain).collect(Collectors.toList());
    }
}
