package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity;

import co.edu.monteria.medicitas.domain.model.enums.TipoServicio;
import jakarta.persistence.*;

/**
 * Entidad JPA para el catálogo de servicios de cada especialidad.
 */
@Entity
@Table(name = "servicios")
public class ServicioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidad_id", nullable = false)
    private EspecialidadEntity especialidad;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoServicio tipo;

    public ServicioEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public EspecialidadEntity getEspecialidad() { return especialidad; }
    public void setEspecialidad(EspecialidadEntity especialidad) { this.especialidad = especialidad; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoServicio getTipo() { return tipo; }
    public void setTipo(TipoServicio tipo) { this.tipo = tipo; }
}
