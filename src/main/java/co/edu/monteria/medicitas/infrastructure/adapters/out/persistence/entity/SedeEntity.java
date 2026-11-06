package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;

/**
 * Entidad JPA para la tabla 'sedes': cada una de las sedes físicas de una IPS.
 */
@Entity
@Table(name = "sedes")
public class SedeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ips_id", nullable = false)
    private IpsEntity ips;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(length = 200)
    private String direccion;

    public SedeEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public IpsEntity getIps() { return ips; }
    public void setIps(IpsEntity ips) { this.ips = ips; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
