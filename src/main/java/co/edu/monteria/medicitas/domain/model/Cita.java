package co.edu.monteria.medicitas.domain.model;

import co.edu.monteria.medicitas.domain.model.enums.EstadoCita;
import co.edu.monteria.medicitas.domain.model.enums.TipoCita;
import java.time.LocalDateTime;

public class Cita {
    private Long id;
    private Usuario paciente;
    private DisponibilidadMedico disponibilidad;
    private Ips ips;
    private TipoCita tipo;
    private EstadoCita estado;
    private String observaciones;
    private LocalDateTime fechaCreacion;

    public Cita() {}

    public Cita(Long id, Usuario paciente, DisponibilidadMedico disponibilidad, Ips ips,
                TipoCita tipo, EstadoCita estado, String observaciones, LocalDateTime fechaCreacion) {
        this.id = id;
        this.paciente = paciente;
        this.disponibilidad = disponibilidad;
        this.ips = ips;
        this.tipo = tipo;
        this.estado = estado;
        this.observaciones = observaciones;
        this.fechaCreacion = fechaCreacion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getPaciente() { return paciente; }
    public void setPaciente(Usuario paciente) { this.paciente = paciente; }

    public DisponibilidadMedico getDisponibilidad() { return disponibilidad; }
    public void setDisponibilidad(DisponibilidadMedico disponibilidad) { this.disponibilidad = disponibilidad; }

    public Ips getIps() { return ips; }
    public void setIps(Ips ips) { this.ips = ips; }

    public TipoCita getTipo() { return tipo; }
    public void setTipo(TipoCita tipo) { this.tipo = tipo; }

    public EstadoCita getEstado() { return estado; }
    public void setEstado(EstadoCita estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
