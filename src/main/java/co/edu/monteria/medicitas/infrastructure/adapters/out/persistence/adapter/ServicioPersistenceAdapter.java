package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.adapter;

import co.edu.monteria.medicitas.application.ports.out.ServicioRepositoryPort;
import co.edu.monteria.medicitas.domain.model.Servicio;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.mapper.DomainEntityMapper;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository.ServicioJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class ServicioPersistenceAdapter implements ServicioRepositoryPort {

    private final ServicioJpaRepository repository;
    private final DomainEntityMapper mapper;

    public ServicioPersistenceAdapter(ServicioJpaRepository repository, DomainEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Servicio> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::toServicioDomain);
    }

    @Override
    public List<Servicio> buscarPorEspecialidadId(Long especialidadId) {
        return repository.findByEspecialidadIdOrderByIdAsc(especialidadId).stream()
                .map(mapper::toServicioDomain).toList();
    }
}
