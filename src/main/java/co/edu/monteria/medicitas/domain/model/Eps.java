package co.edu.monteria.medicitas.domain.model;

public class Eps {
    private Long id;
    private String nombre;
    private String codigoBdua;
    private Boolean activa;

    public Eps() {}

    public Eps(Long id, String nombre, String codigoBdua, Boolean activa) {
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
