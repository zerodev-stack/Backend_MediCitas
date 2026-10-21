package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity;

import co.edu.monteria.medicitas.domain.model.enums.EstadoCita;
import co.edu.monteria.medicitas.domain.model.enums.TipoCita;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA para agendamiento de citas médicas o exámentes.
 */
@Entity
@Table(name = "citas")
public class CitaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private UsuarioEntity paciente;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disponibilidad_id", nullable = false, unique = true)
    private DisponibilidadMedicoEntity disponibilidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ips_id", nullable = false)
    private IpsEntity ips;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCita tipo = TipoCita.CONSULTA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado = EstadoCita.PROGRAMADA;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public CitaEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UsuarioEntity getPaciente() { return paciente; }
    public void setPaciente(UsuarioEntity paciente) { this.paciente = paciente; }

    public DisponibilidadMedicoEntity getDisponibilidad() { return disponibilidad; }
    public void setDisponibilidad(DisponibilidadMedicoEntity disponibilidad) { this.disponibilidad = disponibilidad; }

    public IpsEntity getIps() { return ips; }
    public void setIps(IpsEntity ips) { this.ips = ips; }

    public TipoCita getTipo() { return tipo; }
    public void setTipo(TipoCita tipo) { this.tipo = tipo; }

    public EstadoCita getEstado() { return estado; }
    public void setEstado(EstadoCita estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
