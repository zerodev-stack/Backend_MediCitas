package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository;

import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.ServicioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicioJpaRepository extends JpaRepository<ServicioEntity, Long> {

    List<ServicioEntity> findByEspecialidadIdOrderByIdAsc(Long especialidadId);
}
