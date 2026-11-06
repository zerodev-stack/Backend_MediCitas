package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity;

import co.edu.monteria.medicitas.domain.model.enums.EstadoBloque;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entidad JPA para los bloques de disponibilidad. Cada bloque genera N filas en
 * 'disponibilidades_medicas', una por cupo.
 */
@Entity
@Table(name = "bloques_disponibilidad")
public class BloqueDisponibilidadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    private MedicoEntity medico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false)
    private ServicioEntity servicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sede_id", nullable = false)
    private SedeEntity sede;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "duracion_cupo_minutos", nullable = false)
    private Integer duracionCupoMinutos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoBloque estado = EstadoBloque.ACTIVO;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public BloqueDisponibilidadEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public MedicoEntity getMedico() { return medico; }
    public void setMedico(MedicoEntity medico) { this.medico = medico; }

    public ServicioEntity getServicio() { return servicio; }
    public void setServicio(ServicioEntity servicio) { this.servicio = servicio; }

    public SedeEntity getSede() { return sede; }
    public void setSede(SedeEntity sede) { this.sede = sede; }

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
