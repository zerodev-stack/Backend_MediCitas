package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;

/**
 * Entidad JPA para la tabla 'medicos'. Vincula un usuario con rol médico a su especialidad,
 * su IPS de adscripción y su registro profesional ReTHUS.
 */
@Entity
@Table(name = "medicos")
public class MedicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Cuenta de usuario asociada (OneToOne) */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private UsuarioEntity usuario;

    /** Especialidad en la que ejerce el profesional */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidad_id", nullable = false)
    private EspecialidadEntity especialidad;

    /** IPS prestadora donde atiende el médico */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ips_id", nullable = false)
    private IpsEntity ips;

    /** Número de tarjeta profesional o registro médico ReTHUS */
    @Column(name = "registro_medico", nullable = false, unique = true, length = 50)
    private String registroMedico;

    public MedicoEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UsuarioEntity getUsuario() { return usuario; }
    public void setUsuario(UsuarioEntity usuario) { this.usuario = usuario; }

    public EspecialidadEntity getEspecialidad() { return especialidad; }
    public void setEspecialidad(EspecialidadEntity especialidad) { this.especialidad = especialidad; }

    public IpsEntity getIps() { return ips; }
    public void setIps(IpsEntity ips) { this.ips = ips; }

    public String getRegistroMedico() { return registroMedico; }
    public void setRegistroMedico(String registroMedico) { this.registroMedico = registroMedico; }
}
