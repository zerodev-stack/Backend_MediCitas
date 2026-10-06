package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.adapter;

import co.edu.monteria.medicitas.application.ports.out.MedicoRepositoryPort;
import co.edu.monteria.medicitas.domain.model.Medico;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.MedicoEntity;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.mapper.DomainEntityMapper;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository.MedicoJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class MedicoPersistenceAdapter implements MedicoRepositoryPort {

    private final MedicoJpaRepository repository;
    private final DomainEntityMapper mapper;

    public MedicoPersistenceAdapter(MedicoJpaRepository repository, DomainEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Medico guardar(Medico medico) {
        MedicoEntity entity = mapper.toMedicoEntity(medico);
        MedicoEntity guardado = repository.save(entity);
        return mapper.toMedicoDomain(guardado);
    }

    @Override
    public Optional<Medico> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::toMedicoDomain);
    }

    @Override
    public Optional<Medico> buscarPorUsuarioId(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId).map(mapper::toMedicoDomain);
    }
}
