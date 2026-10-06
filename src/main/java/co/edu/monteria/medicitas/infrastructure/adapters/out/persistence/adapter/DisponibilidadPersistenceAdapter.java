package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.adapter;

import co.edu.monteria.medicitas.application.ports.out.DisponibilidadRepositoryPort;
import co.edu.monteria.medicitas.domain.model.DisponibilidadMedico;
import co.edu.monteria.medicitas.domain.model.enums.EstadoDisponibilidad;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.DisponibilidadMedicoEntity;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.mapper.DomainEntityMapper;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository.DisponibilidadMedicoJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
public class DisponibilidadPersistenceAdapter implements DisponibilidadRepositoryPort {

    private final DisponibilidadMedicoJpaRepository repository;
    private final DomainEntityMapper mapper;

    public DisponibilidadPersistenceAdapter(DisponibilidadMedicoJpaRepository repository, DomainEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public DisponibilidadMedico guardar(DisponibilidadMedico disponibilidad) {
        DisponibilidadMedicoEntity entity = mapper.toDisponibilidadEntity(disponibilidad);
        DisponibilidadMedicoEntity guardado = repository.save(entity);
        return mapper.toDisponibilidadDomain(guardado);
    }

    @Override
    public Optional<DisponibilidadMedico> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::toDisponibilidadDomain);
    }

    @Override
    public List<DisponibilidadMedico> buscarDisponibles(Long especialidadId, Long ipsId, LocalDate fecha) {
        return repository.findDisponibles(especialidadId, ipsId, EstadoDisponibilidad.LIBRE, fecha)
                .stream().map(mapper::toDisponibilidadDomain).collect(Collectors.toList());
    }

    @Override
    public List<DisponibilidadMedico> buscarPorMedicoId(Long medicoId) {
        return repository.findByMedicoIdOrderByFechaAscHoraInicioAsc(medicoId)
                .stream().map(mapper::toDisponibilidadDomain).collect(Collectors.toList());
    }

    @Override
    public boolean transicionarEstadoSiCoincide(Long disponibilidadId,
                                                EstadoDisponibilidad esperado,
                                                EstadoDisponibilidad nuevo) {
        return repository.transicionarEstadoSiCoincide(disponibilidadId, esperado, nuevo) == 1;
    }
}
