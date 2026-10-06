package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository;

import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.IpsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IpsJpaRepository extends JpaRepository<IpsEntity, Long> {
    
    @Query("SELECT ei.ips FROM EpsIpsEntity ei WHERE ei.eps.id = :epsId AND ei.convenioActivo = true")
    List<IpsEntity> findByEpsId(@Param("epsId") Long epsId);
}
