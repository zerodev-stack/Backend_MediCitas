package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;

/**
 * Entidad JPA que representa una EPS (Entidad Promotora de Salud) en Colombia.
 * Las EPS administran la afiliación de los usuarios y garantizan la prestación del POS.
 */
@Entity
@Table(name = "eps")
public class EpsEntity {

    /** Identificador único autonumérico de la EPS */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre oficial de la EPS (ej. EPS Mutualser) */
    @Column(nullable = false, length = 120)
    private String nombre;

    /** Código de registro en la BDUA (Base de Datos Única de Afiliados - ej. EPS008) */
    @Column(name = "codigo_bdua", nullable = false, unique = true, length = 20)
    private String codigoBdua;

    /** Indica si la EPS se encuentra activa y operando en la red regional de Montería */
    @Column(nullable = false)
    private Boolean activa = true;

    public EpsEntity() {}

    public EpsEntity(Long id, String nombre, String codigoBdua, Boolean activa) {
        this.id = id;
        this.nombre = nombre;
        this.codigoBdua = codigoBdua;
        this.activa = activa;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCodigoBdua() { return codigoBdua; }
    public void setCodigoBdua(String codigoBdua) { this.codigoBdua = codigoBdua; }

    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
}
