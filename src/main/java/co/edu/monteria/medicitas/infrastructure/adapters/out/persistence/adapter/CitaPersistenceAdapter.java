package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.adapter;

import co.edu.monteria.medicitas.application.ports.out.CitaRepositoryPort;
import co.edu.monteria.medicitas.domain.model.Cita;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.CitaEntity;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.mapper.DomainEntityMapper;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository.CitaJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
public class CitaPersistenceAdapter implements CitaRepositoryPort {

    private final CitaJpaRepository repository;
    private final DomainEntityMapper mapper;

    public CitaPersistenceAdapter(CitaJpaRepository repository, DomainEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Cita guardar(Cita cita) {
        CitaEntity entity = mapper.toCitaEntity(cita);
        CitaEntity guardado = repository.save(entity);
        return mapper.toCitaDomain(guardado);
    }

    @Override
    public Optional<Cita> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::toCitaDomain);
    }

    @Override
    public List<Cita> buscarPorPacienteId(Long pacienteId) {
        return repository.findByPacienteIdOrderByFechaCreacionDesc(pacienteId)
                .stream().map(mapper::toCitaDomain).collect(Collectors.toList());
    }

    @Override
    public List<Cita> buscarPorMedicoId(Long medicoId) {
        return repository.findByDisponibilidadMedicoIdOrderByDisponibilidadFechaAsc(medicoId)
                .stream().map(mapper::toCitaDomain).collect(Collectors.toList());
    }
}
