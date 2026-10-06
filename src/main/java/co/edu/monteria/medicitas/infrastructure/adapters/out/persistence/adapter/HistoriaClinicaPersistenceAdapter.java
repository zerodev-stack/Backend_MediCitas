package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.adapter;

import co.edu.monteria.medicitas.application.ports.out.HistoriaClinicaRepositoryPort;
import co.edu.monteria.medicitas.domain.model.HistoriaClinica;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.HistoriaClinicaEntity;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.mapper.DomainEntityMapper;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository.HistoriaClinicaJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
public class HistoriaClinicaPersistenceAdapter implements HistoriaClinicaRepositoryPort {

    private final HistoriaClinicaJpaRepository repository;
    private final DomainEntityMapper mapper;

    public HistoriaClinicaPersistenceAdapter(HistoriaClinicaJpaRepository repository, DomainEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public HistoriaClinica guardar(HistoriaClinica historia) {
        HistoriaClinicaEntity entity = mapper.toHistoriaClinicaEntity(historia);
        HistoriaClinicaEntity guardada = repository.save(entity);
        return mapper.toHistoriaClinicaDomain(guardada);
    }

    @Override
    public Optional<HistoriaClinica> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::toHistoriaClinicaDomain);
    }

    @Override
    public List<HistoriaClinica> buscarPorPacienteId(Long pacienteId) {
        return repository.findByPacienteIdOrderByFechaRegistroDesc(pacienteId)
                .stream().map(mapper::toHistoriaClinicaDomain).collect(Collectors.toList());
    }
}
