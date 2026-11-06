package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity;

import co.edu.monteria.medicitas.domain.model.enums.EstadoColaEspera;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA para la cola de espera FIFO cuando no hay disponibilidades médicas libres.
 * Al liberarse un cupo (por cancelación o nueva disponibilidad), el sistema asigna automáticamente
 * al paciente con la solicitud de espera más antigua (FIFO) y genera la notificación respectiva.
 */
@Entity
@Table(name = "colas_espera")
public class ColaEsperaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private UsuarioEntity paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidad_id", nullable = false)
    private EspecialidadEntity especialidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ips_id", nullable = false)
    private IpsEntity ips;

    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoColaEspera estado = EstadoColaEspera.EN_ESPERA;

    /** Servicio solicitado (opcional) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id")
    private ServicioEntity servicio;

    /** Sede solicitada (opcional) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sede_id")
    private SedeEntity sede;

    public ColaEsperaEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UsuarioEntity getPaciente() { return paciente; }
    public void setPaciente(UsuarioEntity paciente) { this.paciente = paciente; }

    public EspecialidadEntity getEspecialidad() { return especialidad; }
    public void setEspecialidad(EspecialidadEntity especialidad) { this.especialidad = especialidad; }

    public IpsEntity getIps() { return ips; }
    public void setIps(IpsEntity ips) { this.ips = ips; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public EstadoColaEspera getEstado() { return estado; }
    public void setEstado(EstadoColaEspera estado) { this.estado = estado; }

    public ServicioEntity getServicio() { return servicio; }
    public void setServicio(ServicioEntity servicio) { this.servicio = servicio; }

    public SedeEntity getSede() { return sede; }
    public void setSede(SedeEntity sede) { this.sede = sede; }
}
