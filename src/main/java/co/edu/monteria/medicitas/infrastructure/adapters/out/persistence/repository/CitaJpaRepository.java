package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository;

import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.CitaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CitaJpaRepository extends JpaRepository<CitaEntity, Long> {
    List<CitaEntity> findByPacienteIdOrderByFechaCreacionDesc(Long pacienteId);
    List<CitaEntity> findByDisponibilidadMedicoIdOrderByDisponibilidadFechaAsc(Long medicoId);
}
