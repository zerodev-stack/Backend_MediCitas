package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository;

import co.edu.monteria.medicitas.domain.model.enums.EstadoBloque;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.BloqueDisponibilidadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BloqueDisponibilidadJpaRepository extends JpaRepository<BloqueDisponibilidadEntity, Long> {

    List<BloqueDisponibilidadEntity> findByMedicoIdAndEstadoOrderByFechaAscHoraInicioAsc(Long medicoId, EstadoBloque estado);

    List<BloqueDisponibilidadEntity> findByMedicoIdAndFechaAndEstado(Long medicoId, LocalDate fecha, EstadoBloque estado);
}
