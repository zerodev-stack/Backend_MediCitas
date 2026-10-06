package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.adapter;

import co.edu.monteria.medicitas.application.ports.out.AutorizacionAccesoRepositoryPort;
import co.edu.monteria.medicitas.domain.model.AutorizacionAcceso;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.AutorizacionAccesoEntity;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.mapper.DomainEntityMapper;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository.AutorizacionAccesoJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
public class AutorizacionAccesoPersistenceAdapter implements AutorizacionAccesoRepositoryPort {

    private final AutorizacionAccesoJpaRepository repository;
    private final DomainEntityMapper mapper;

    public AutorizacionAccesoPersistenceAdapter(AutorizacionAccesoJpaRepository repository, DomainEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public AutorizacionAcceso guardar(AutorizacionAcceso autorizacion) {
        AutorizacionAccesoEntity entity = mapper.toAutorizacionEntity(autorizacion);
        AutorizacionAccesoEntity guardado = repository.save(entity);
        return mapper.toAutorizacionDomain(guardado);
    }

    @Override
    public Optional<AutorizacionAcceso> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::toAutorizacionDomain);
    }

    @Override
    public Optional<AutorizacionAcceso> buscarPorHistoriaEIpsSolicitante(Long historiaId, Long ipsSolicitanteId) {
        return repository.findByHistoriaIdAndIpsSolicitanteId(historiaId, ipsSolicitanteId).map(mapper::toAutorizacionDomain);
    }

    @Override
    public List<AutorizacionAcceso> buscarPorPacienteId(Long pacienteId) {
        return repository.findByHistoriaPacienteId(pacienteId).stream().map(mapper::toAutorizacionDomain).collect(Collectors.toList());
    }
}
