package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository;

import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.EspecialidadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecialidadJpaRepository extends JpaRepository<EspecialidadEntity, Long> {
}
