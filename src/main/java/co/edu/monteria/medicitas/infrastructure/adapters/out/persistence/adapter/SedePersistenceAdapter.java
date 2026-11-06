package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.adapter;

import co.edu.monteria.medicitas.application.ports.out.SedeRepositoryPort;
import co.edu.monteria.medicitas.domain.model.Sede;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.mapper.DomainEntityMapper;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository.SedeJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class SedePersistenceAdapter implements SedeRepositoryPort {

    private final SedeJpaRepository repository;
    private final DomainEntityMapper mapper;

    public SedePersistenceAdapter(SedeJpaRepository repository, DomainEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Sede> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::toSedeDomain);
    }

    @Override
    public List<Sede> buscarPorIpsId(Long ipsId) {
        return repository.findByIpsIdOrderByNombreAsc(ipsId).stream().map(mapper::toSedeDomain).toList();
    }
}
