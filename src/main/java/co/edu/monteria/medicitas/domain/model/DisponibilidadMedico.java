package co.edu.monteria.medicitas.domain.model;

import co.edu.monteria.medicitas.domain.model.enums.EstadoDisponibilidad;
import java.time.LocalDate;
import java.time.LocalTime;

public class DisponibilidadMedico {
    private Long id;
    private Medico medico;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private EstadoDisponibilidad estado;

    public DisponibilidadMedico() {}

    public DisponibilidadMedico(Long id, Medico medico, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, EstadoDisponibilidad estado) {
        this.id = id;
        this.medico = medico;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public EstadoDisponibilidad getEstado() { return estado; }
    public void setEstado(EstadoDisponibilidad estado) { this.estado = estado; }
}
