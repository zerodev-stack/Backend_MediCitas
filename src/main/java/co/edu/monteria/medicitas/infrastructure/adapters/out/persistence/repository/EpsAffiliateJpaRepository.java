package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository;

import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.EpsAffiliateEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EpsAffiliateJpaRepository extends JpaRepository<EpsAffiliateEntity, Long> {

    // La EPS se carga en la misma consulta: quien llama no está dentro de una transacción.
    @EntityGraph(attributePaths = "eps")
    Optional<EpsAffiliateEntity> findByDocumentNumber(String documentNumber);
}
