package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA para la Historia Clínica del paciente.
 * Conforme a la Res. 1995 de 1999 de Colombia, la historia clínica es un documento privado,
 * de reserva legal, que solo puede ser examinado por terceros previa autorización del paciente.
 */
@Entity
@Table(name = "historias_clinicas")
public class HistoriaClinicaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private UsuarioEntity paciente;

    /** IPS donde se generó el registro clínico */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ips_origen_id", nullable = false)
    private IpsEntity ipsOrigen;

    @Column(nullable = false, length = 255)
    private String diagnostico;

    @Lob
    @Column
    private String tratamiento;

    @Lob
    @Column(nullable = false)
    private String contenido;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public HistoriaClinicaEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UsuarioEntity getPaciente() { return paciente; }
    public void setPaciente(UsuarioEntity paciente) { this.paciente = paciente; }

    public IpsEntity getIpsOrigen() { return ipsOrigen; }
    public void setIpsOrigen(IpsEntity ipsOrigen) { this.ipsOrigen = ipsOrigen; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
