package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository;

import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.ServicioMedicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServicioMedicoJpaRepository extends JpaRepository<ServicioMedicoEntity, Long> {

    List<ServicioMedicoEntity> findByMedicoIdOrderByServicioIdAscSedeIdAsc(Long medicoId);

    boolean existsByMedicoIdAndServicioIdAndSedeId(Long medicoId, Long servicioId, Long sedeId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ServicioMedicoEntity s WHERE s.medico.id = :medicoId")
    int eliminarPorMedicoId(@Param("medicoId") Long medicoId);
}
