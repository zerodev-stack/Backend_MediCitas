package co.edu.monteria.medicitas.domain.model;

import co.edu.monteria.medicitas.domain.model.enums.EstadoBloque;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Bloque de atención publicado por un médico: un servicio, en una sede, una fecha y un
 * horario, dividido en cupos de 'duracionCupoMinutos'. Cada cupo es una
 * 'DisponibilidadMedico' que el paciente reserva de forma individual.
 */
public class BloqueDisponibilidad {
    private Long id;
    private Medico medico;
    private Servicio servicio;
    private Sede sede;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer duracionCupoMinutos;
    private EstadoBloque estado;
    private LocalDateTime fechaCreacion;

    public BloqueDisponibilidad() {}

    public BloqueDisponibilidad(Long id, Medico medico, Servicio servicio, Sede sede, LocalDate fecha,
                                LocalTime horaInicio, LocalTime horaFin, Integer duracionCupoMinutos,
                                EstadoBloque estado, LocalDateTime fechaCreacion) {
        this.id = id;
        this.medico = medico;
        this.servicio = servicio;
        this.sede = sede;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.duracionCupoMinutos = duracionCupoMinutos;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    /** Dos bloques del mismo día se cruzan si cada uno empieza antes de que termine el otro. */
    public boolean seCruzaCon(LocalDate otraFecha, LocalTime otroInicio, LocalTime otroFin) {
        return fecha.equals(otraFecha) && horaInicio.isBefore(otroFin) && otroInicio.isBefore(horaFin);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }

    public Servicio getServicio() { return servicio; }
    public void setServicio(Servicio servicio) { this.servicio = servicio; }

    public Sede getSede() { return sede; }
    public void setSede(Sede sede) { this.sede = sede; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public Integer getDuracionCupoMinutos() { return duracionCupoMinutos; }
    public void setDuracionCupoMinutos(Integer duracionCupoMinutos) { this.duracionCupoMinutos = duracionCupoMinutos; }

    public EstadoBloque getEstado() { return estado; }
    public void setEstado(EstadoBloque estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
