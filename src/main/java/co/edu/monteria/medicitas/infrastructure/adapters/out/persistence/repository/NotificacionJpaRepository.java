package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository;

import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.NotificacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacionJpaRepository extends JpaRepository<NotificacionEntity, Long> {
    List<NotificacionEntity> findByUsuarioIdOrderByFechaEnvioDesc(Long usuarioId);
}
