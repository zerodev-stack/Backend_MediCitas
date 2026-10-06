package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.adapter;

import co.edu.monteria.medicitas.application.ports.out.NotificacionRepositoryPort;
import co.edu.monteria.medicitas.domain.model.Notificacion;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.NotificacionEntity;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.mapper.DomainEntityMapper;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository.NotificacionJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
public class NotificacionPersistenceAdapter implements NotificacionRepositoryPort {

    private final NotificacionJpaRepository repository;
    private final DomainEntityMapper mapper;

    public NotificacionPersistenceAdapter(NotificacionJpaRepository repository, DomainEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Notificacion guardar(Notificacion notificacion) {
        NotificacionEntity entity = mapper.toNotificacionEntity(notificacion);
        NotificacionEntity guardada = repository.save(entity);
        return mapper.toNotificacionDomain(guardada);
    }

    @Override
    public Optional<Notificacion> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::toNotificacionDomain);
    }

    @Override
    public List<Notificacion> buscarPorUsuarioId(Long usuarioId) {
        return repository.findByUsuarioIdOrderByFechaEnvioDesc(usuarioId)
                .stream().map(mapper::toNotificacionDomain).collect(Collectors.toList());
    }
}
