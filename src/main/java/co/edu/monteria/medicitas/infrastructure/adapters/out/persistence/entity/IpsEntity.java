package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;

/**
 * Entidad JPA que representa una IPS (Institución Prestadora de Servicios de Salud) en Montería.
 * Las IPS son las clínicas, centros médicos u hospitales donde los médicos atienden las citas.
 */
@Entity
@Table(name = "ips")
public class IpsEntity {

    /** Identificador único de la IPS */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre de la clínica o centro de salud */
    @Column(nullable = false, length = 150)
    private String nombre;

    /** Número de Identificación Tributaria (NIT) de la IPS */
    @Column(nullable = false, unique = true, length = 20)
    private String nit;

    /** Dirección física de la sede en Montería (ej. Calle 29 # 14-20) */
    @Column(nullable = false, length = 200)
    private String direccion;

    /** Ciudad sede (por defecto Montería, Córdoba) */
    @Column(nullable = false, length = 80)
    private String ciudad = "Montería";

    /** Teléfono de contacto de la IPS */
    @Column(length = 30)
    private String telefono;

    public IpsEntity() {}

    public IpsEntity(Long id, String nombre, String nit, String direccion, String ciudad, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.nit = nit;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.telefono = telefono;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
