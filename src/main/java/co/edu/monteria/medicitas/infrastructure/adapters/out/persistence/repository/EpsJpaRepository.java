package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository;

import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.EpsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EpsJpaRepository extends JpaRepository<EpsEntity, Long> {
    Optional<EpsEntity> findByCodigoBdua(String codigoBdua);
}
