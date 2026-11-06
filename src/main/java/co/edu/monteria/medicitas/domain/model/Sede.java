package co.edu.monteria.medicitas.domain.model;

/**
 * Sede física de una IPS (por ejemplo, "Sede Centro" o "Sede Norte").
 */
public class Sede {
    private Long id;
    private Ips ips;
    private String nombre;
    private String direccion;

    public Sede() {}

    public Sede(Long id, Ips ips, String nombre, String direccion) {
        this.id = id;
        this.ips = ips;
        this.nombre = nombre;
        this.direccion = direccion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Ips getIps() { return ips; }
    public void setIps(Ips ips) { this.ips = ips; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
