package co.edu.monteria.medicitas.domain.model;

import co.edu.monteria.medicitas.domain.model.enums.EstadoColaEspera;
import java.time.LocalDateTime;

public class ColaEspera {
    private Long id;
    private Usuario paciente;
    private Especialidad especialidad;
    private Ips ips;
    private LocalDateTime fechaSolicitud;
    private EstadoColaEspera estado;

    public ColaEspera() {}

    public ColaEspera(Long id, Usuario paciente, Especialidad especialidad, Ips ips, LocalDateTime fechaSolicitud, EstadoColaEspera estado) {
        this.id = id;
        this.paciente = paciente;
        this.especialidad = especialidad;
        this.ips = ips;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getPaciente() { return paciente; }
    public void setPaciente(Usuario paciente) { this.paciente = paciente; }

    public Especialidad getEspecialidad() { return especialidad; }
    public void setEspecialidad(Especialidad especialidad) { this.especialidad = especialidad; }

    public Ips getIps() { return ips; }
    public void setIps(Ips ips) { this.ips = ips; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public EstadoColaEspera getEstado() { return estado; }
    public void setEstado(EstadoColaEspera estado) { this.estado = estado; }
}
