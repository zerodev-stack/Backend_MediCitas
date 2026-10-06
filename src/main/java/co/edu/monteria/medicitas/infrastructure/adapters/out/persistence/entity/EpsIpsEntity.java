package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;

/**
 * Entidad JPA que representa la relación y convenio entre una EPS y una IPS.
 */
@Entity
@Table(name = "eps_ips")
public class EpsIpsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eps_id", nullable = false)
    private EpsEntity eps;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ips_id", nullable = false)
    private IpsEntity ips;

    @Column(name = "convenio_activo", nullable = false)
    private Boolean convenioActivo = true;

    public EpsIpsEntity() {}

    public EpsIpsEntity(Long id, EpsEntity eps, IpsEntity ips, Boolean convenioActivo) {
        this.id = id;
        this.eps = eps;
        this.ips = ips;
        this.convenioActivo = convenioActivo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public EpsEntity getEps() { return eps; }
    public void setEps(EpsEntity eps) { this.eps = eps; }

    public IpsEntity getIps() { return ips; }
    public void setIps(IpsEntity ips) { this.ips = ips; }

    public Boolean getConvenioActivo() { return convenioActivo; }
    public void setConvenioActivo(Boolean convenioActivo) { this.convenioActivo = convenioActivo; }
}
