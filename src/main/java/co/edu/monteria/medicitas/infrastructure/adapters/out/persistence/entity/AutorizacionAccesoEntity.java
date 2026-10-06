package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity;

import co.edu.monteria.medicitas.domain.model.enums.EstadoAutorizacion;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA para gestionar el consentimiento informado y autorización explícita
 * conferida por un paciente para que una IPS solicitante pueda consultar la historia
 * clínica generada en otra IPS de origen (Resoluciones 1995/1999 y 3280/2018 de Colombia).
 */
@Entity
@Table(name = "autorizaciones_acceso", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"historia_id", "ips_solicitante_id"})
})
public class AutorizacionAccesoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "historia_id", nullable = false)
    private HistoriaClinicaEntity historia;

    /** IPS que solicita acceso a la historia clínica de otra IPS */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ips_solicitante_id", nullable = false)
    private IpsEntity ipsSolicitante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAutorizacion estado = EstadoAutorizacion.HABILITADO;

    @Column(name = "fecha_autorizacion", nullable = false, updatable = false)
    private LocalDateTime fechaAutorizacion = LocalDateTime.now();

    @Column(name = "fecha_revocacion")
    private LocalDateTime fechaRevocacion;

    public AutorizacionAccesoEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public HistoriaClinicaEntity getHistoria() { return historia; }
    public void setHistoria(HistoriaClinicaEntity historia) { this.historia = historia; }

    public IpsEntity getIpsSolicitante() { return ipsSolicitante; }
    public void setIpsSolicitante(IpsEntity ipsSolicitante) { this.ipsSolicitante = ipsSolicitante; }

    public EstadoAutorizacion getEstado() { return estado; }
    public void setEstado(EstadoAutorizacion estado) { this.estado = estado; }

    public LocalDateTime getFechaAutorizacion() { return fechaAutorizacion; }
    public void setFechaAutorizacion(LocalDateTime fechaAutorizacion) { this.fechaAutorizacion = fechaAutorizacion; }

    public LocalDateTime getFechaRevocacion() { return fechaRevocacion; }
    public void setFechaRevocacion(LocalDateTime fechaRevocacion) { this.fechaRevocacion = fechaRevocacion; }
}
