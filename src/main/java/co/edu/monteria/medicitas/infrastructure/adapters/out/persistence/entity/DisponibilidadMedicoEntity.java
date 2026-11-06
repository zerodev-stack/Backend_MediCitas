package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity;

import co.edu.monteria.medicitas.domain.model.enums.EstadoDisponibilidad;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entidad JPA para franjas horarias de disponibilidad médica.
 * Representa los cupos de atención configurados por un médico para agendamiento.
 */
@Entity
@Table(name = "disponibilidades_medicas")
public class DisponibilidadMedicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    private MedicoEntity medico;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoDisponibilidad estado = EstadoDisponibilidad.LIBRE;

    /** Bloque que generó este cupo; null en las franjas sueltas */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bloque_id")
    private BloqueDisponibilidadEntity bloque;

    public DisponibilidadMedicoEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public MedicoEntity getMedico() { return medico; }
    public void setMedico(MedicoEntity medico) { this.medico = medico; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public EstadoDisponibilidad getEstado() { return estado; }
    public void setEstado(EstadoDisponibilidad estado) { this.estado = estado; }

    public BloqueDisponibilidadEntity getBloque() { return bloque; }
    public void setBloque(BloqueDisponibilidadEntity bloque) { this.bloque = bloque; }
}
