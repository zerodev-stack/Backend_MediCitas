package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository;

import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.MedicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MedicoJpaRepository extends JpaRepository<MedicoEntity, Long> {
    Optional<MedicoEntity> findByUsuarioId(Long usuarioId);
    List<MedicoEntity> findByEspecialidadIdAndIpsId(Long especialidadId, Long ipsId);
}
