package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository;

import co.edu.monteria.medicitas.domain.model.enums.EstadoDisponibilidad;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.DisponibilidadMedicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DisponibilidadMedicoJpaRepository extends JpaRepository<DisponibilidadMedicoEntity, Long> {

    @Query("SELECT d FROM DisponibilidadMedicoEntity d " +
           "WHERE d.medico.especialidad.id = :especialidadId " +
           "AND d.medico.ips.id = :ipsId " +
           "AND d.estado = :estado " +
           "AND (:fecha IS NULL OR d.fecha = :fecha) " +
           "ORDER BY d.fecha ASC, d.horaInicio ASC")
    List<DisponibilidadMedicoEntity> findDisponibles(
            @Param("especialidadId") Long especialidadId,
            @Param("ipsId") Long ipsId,
            @Param("estado") EstadoDisponibilidad estado,
            @Param("fecha") LocalDate fecha
    );

    List<DisponibilidadMedicoEntity> findByMedicoIdOrderByFechaAscHoraInicioAsc(Long medicoId);

    List<DisponibilidadMedicoEntity> findByBloqueIdOrderByHoraInicioAsc(Long bloqueId);

    List<DisponibilidadMedicoEntity> findByMedicoIdAndEstadoAndFechaGreaterThanEqualOrderByFechaAscHoraInicioAsc(
            Long medicoId, EstadoDisponibilidad estado, LocalDate desde);

    @Query("SELECT d FROM DisponibilidadMedicoEntity d " +
           "WHERE d.medico.id = :medicoId AND d.fecha = :fecha " +
           "AND d.bloque IS NULL AND d.estado <> :excluido")
    List<DisponibilidadMedicoEntity> findSueltasDeMedicoEnFecha(
            @Param("medicoId") Long medicoId,
            @Param("fecha") LocalDate fecha,
            @Param("excluido") EstadoDisponibilidad excluido);

    /**
     * Transición de estado condicionada (compare-and-set).
     *
     * Devuelve 1 solo si la fila estaba en 'estadoEsperado'. Dos pacientes que intenten
     * reservar el mismo cupo a la vez ejecutan esta sentencia en serie: el primero obtiene
     * 1, el segundo 0, y ese 0 es la prueba de que perdió la carrera. Evita depender de la
     * lectura previa del estado, que siempre es susceptible a condiciones de carrera.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE DisponibilidadMedicoEntity d SET d.estado = :nuevo " +
           "WHERE d.id = :id AND d.estado = :esperado")
    int transicionarEstadoSiCoincide(
            @Param("id") Long id,
            @Param("esperado") EstadoDisponibilidad esperado,
            @Param("nuevo") EstadoDisponibilidad nuevo);
}
