package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository;

import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.AutorizacionAccesoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AutorizacionAccesoJpaRepository extends JpaRepository<AutorizacionAccesoEntity, Long> {
    Optional<AutorizacionAccesoEntity> findByHistoriaIdAndIpsSolicitanteId(Long historiaId, Long ipsSolicitanteId);
    List<AutorizacionAccesoEntity> findByHistoriaId(Long historiaId);
    List<AutorizacionAccesoEntity> findByHistoriaPacienteId(Long pacienteId);
}
