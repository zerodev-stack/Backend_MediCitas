package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository;

import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.HistoriaClinicaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistoriaClinicaJpaRepository extends JpaRepository<HistoriaClinicaEntity, Long> {
    List<HistoriaClinicaEntity> findByPacienteIdOrderByFechaRegistroDesc(Long pacienteId);
}
