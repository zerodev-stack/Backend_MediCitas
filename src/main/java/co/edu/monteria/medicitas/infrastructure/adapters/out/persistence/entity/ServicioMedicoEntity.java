package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;

/**
 * Entidad JPA para 'medico_servicios': qué servicio ofrece cada médico y en qué sede.
 */
@Entity
@Table(name = "medico_servicios")
public class ServicioMedicoEntity {

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

    public ServicioMedicoEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public MedicoEntity getMedico() { return medico; }
    public void setMedico(MedicoEntity medico) { this.medico = medico; }

    public ServicioEntity getServicio() { return servicio; }
    public void setServicio(ServicioEntity servicio) { this.servicio = servicio; }

    public SedeEntity getSede() { return sede; }
    public void setSede(SedeEntity sede) { this.sede = sede; }
}
