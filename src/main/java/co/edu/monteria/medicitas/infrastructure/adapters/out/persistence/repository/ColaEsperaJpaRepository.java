package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository;

import co.edu.monteria.medicitas.domain.model.enums.EstadoColaEspera;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.ColaEsperaEntity;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ColaEsperaJpaRepository extends JpaRepository<ColaEsperaEntity, Long> {

    List<ColaEsperaEntity> findByPacienteIdOrderByFechaSolicitudDesc(Long pacienteId);

    /**
     * Cabecera de la cola FIFO tomada con bloqueo de escritura.
     *
     * POR QUÉ EL BLOQUEO: la primera posición de la cola es un recurso disputado. Sin
     * bloqueo, dos transacciones concurrentes (por ejemplo, dos pacientes que cancelan
     * a la vez y liberan cupos del mismo médico) leerían la MISMA fila y generarían dos
     * citas para un solo cupo.
     *
     * El valor -2 de 'jakarta.persistence.lock.timeout' corresponde a SKIP LOCKED: hace
     * que la segunda transacción salté la fila ya bloqueada y tome la siguiente en lugar
     * de esperar, de modo que N liberaciones simultáneas atienden a N pacientes distintos.
     * En motores sin soporte (H2, usado en pruebas) el dialecto degrada a un FOR UPDATE
     * simple, que sigue siendo correcto aunque serializa.
     *
     * 'Pageable' traduce la palabra clave LIMIT 1, que Spring Data no admite en un
     * método derivado.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("SELECT c FROM ColaEsperaEntity c " +
           "WHERE c.especialidad.id = :especialidadId " +
           "  AND c.ips.id = :ipsId " +
           "  AND c.estado = :estado " +
           "ORDER BY c.fechaSolicitud ASC, c.id ASC")
    List<ColaEsperaEntity> findFifoHeadForUpdate(
            @Param("especialidadId") Long especialidadId,
            @Param("ipsId") Long ipsId,
            @Param("estado") EstadoColaEspera estado,
            Pageable pageable);

    boolean existsByPacienteIdAndEspecialidadIdAndIpsIdAndEstado(
            Long pacienteId, Long especialidadId, Long ipsId, EstadoColaEspera estado);
}
