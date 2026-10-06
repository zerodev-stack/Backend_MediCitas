package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.adapter;

import co.edu.monteria.medicitas.application.ports.out.EpsRepositoryPort;
import co.edu.monteria.medicitas.domain.model.Eps;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.mapper.DomainEntityMapper;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository.EpsJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
public class EpsPersistenceAdapter implements EpsRepositoryPort {

    private final EpsJpaRepository repository;
    private final DomainEntityMapper mapper;

    public EpsPersistenceAdapter(EpsJpaRepository repository, DomainEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<Eps> obtenerTodas() {
        return repository.findAll().stream().map(mapper::toEpsDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Eps> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::toEpsDomain);
    }

    @Override
    public Optional<Eps> buscarPorCodigoBdua(String codigoBdua) {
        return repository.findByCodigoBdua(codigoBdua).map(mapper::toEpsDomain);
    }
}
