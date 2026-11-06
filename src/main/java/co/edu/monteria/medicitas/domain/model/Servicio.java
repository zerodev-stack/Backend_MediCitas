package co.edu.monteria.medicitas.domain.model;

import co.edu.monteria.medicitas.domain.model.enums.TipoServicio;

/**
 * Servicio del catálogo de una especialidad (por ejemplo, "Retiro de suturas" en
 * Medicina General). El médico elige cuáles ofrece y en qué sedes.
 */
public class Servicio {
    private Long id;
    private Especialidad especialidad;
    private String nombre;
    private TipoServicio tipo;

    public Servicio() {}

    public Servicio(Long id, Especialidad especialidad, String nombre, TipoServicio tipo) {
        this.id = id;
        this.especialidad = especialidad;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Especialidad getEspecialidad() { return especialidad; }
    public void setEspecialidad(Especialidad especialidad) { this.especialidad = especialidad; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoServicio getTipo() { return tipo; }
    public void setTipo(TipoServicio tipo) { this.tipo = tipo; }
}
