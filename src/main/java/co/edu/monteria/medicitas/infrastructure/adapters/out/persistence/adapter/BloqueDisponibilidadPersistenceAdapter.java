package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.adapter;

import co.edu.monteria.medicitas.application.ports.out.BloqueDisponibilidadRepositoryPort;
import co.edu.monteria.medicitas.domain.model.BloqueDisponibilidad;
import co.edu.monteria.medicitas.domain.model.enums.EstadoBloque;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.BloqueDisponibilidadEntity;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.mapper.DomainEntityMapper;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository.BloqueDisponibilidadJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class BloqueDisponibilidadPersistenceAdapter implements BloqueDisponibilidadRepositoryPort {

    private final BloqueDisponibilidadJpaRepository repository;
    private final DomainEntityMapper mapper;

    public BloqueDisponibilidadPersistenceAdapter(BloqueDisponibilidadJpaRepository repository,
                                                  DomainEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public BloqueDisponibilidad guardar(BloqueDisponibilidad bloque) {
        BloqueDisponibilidadEntity guardado = repository.save(mapper.toBloqueEntity(bloque));
        return mapper.toBloqueDomain(guardado);
    }

    @Override
    public Optional<BloqueDisponibilidad> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::toBloqueDomain);
    }

    @Override
    public List<BloqueDisponibilidad> buscarPorMedicoYEstado(Long medicoId, EstadoBloque estado) {
        return repository.findByMedicoIdAndEstadoOrderByFechaAscHoraInicioAsc(medicoId, estado).stream()
                .map(mapper::toBloqueDomain).toList();
    }

    @Override
    public List<BloqueDisponibilidad> buscarPorMedicoFechaYEstado(Long medicoId, LocalDate fecha, EstadoBloque estado) {
        return repository.findByMedicoIdAndFechaAndEstado(medicoId, fecha, estado).stream()
                .map(mapper::toBloqueDomain).toList();
    }
}
