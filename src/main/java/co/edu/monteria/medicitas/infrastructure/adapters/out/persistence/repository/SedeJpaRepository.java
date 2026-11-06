package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository;

import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.SedeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SedeJpaRepository extends JpaRepository<SedeEntity, Long> {

    List<SedeEntity> findByIpsIdOrderByNombreAsc(Long ipsId);
}
