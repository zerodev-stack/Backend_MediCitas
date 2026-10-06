package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.adapter;

import co.edu.monteria.medicitas.application.ports.out.EspecialidadRepositoryPort;
import co.edu.monteria.medicitas.domain.model.Especialidad;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.mapper.DomainEntityMapper;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository.EspecialidadJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
public class EspecialidadPersistenceAdapter implements EspecialidadRepositoryPort {

    private final EspecialidadJpaRepository repository;
    private final DomainEntityMapper mapper;

    public EspecialidadPersistenceAdapter(EspecialidadJpaRepository repository, DomainEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<Especialidad> obtenerTodas() {
        return repository.findAll().stream().map(mapper::toEspecialidadDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Especialidad> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::toEspecialidadDomain);
    }
}
