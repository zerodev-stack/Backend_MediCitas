package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity;

import co.edu.monteria.medicitas.domain.model.enums.RolUsuario;
import co.edu.monteria.medicitas.domain.model.enums.TipoDocumento;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA para la tabla 'usuarios'.
 * Almacena la información de cuenta para Pacientes, Médicos y Administradores.
 */
@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Cédula o documento de identidad único del paciente o médico */
    @Column(name = "num_documento", nullable = false, unique = true, length = 30)
    private String numDocumento;

    /** Tipo de documento: CC (Cédula de Ciudadanía), TI (Tarjeta de Identidad), CE, PASAPORTE */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 10)
    private TipoDocumento tipoDocumento;

    /** Nombre completo del usuario */
    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    /** Correo electrónico único utilizado como credencial de inicio de sesión */
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    /** Contraseña encriptada mediante algoritmo BCrypt */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /** Rol del sistema: ROLE_PACIENTE, ROLE_MEDICO, ROLE_ADMIN */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolUsuario rol;

    /** EPS a la que está afiliado el paciente. Nullable para usuarios médicos/admin. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eps_id")
    private EpsEntity eps;

    /** Número de teléfono celular o fijo para notificaciones */
    @Column(length = 30)
    private String telefono;

    /** Fecha y hora exacta de registro en la plataforma */
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public UsuarioEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumDocumento() { return numDocumento; }
    public void setNumDocumento(String numDocumento) { this.numDocumento = numDocumento; }

    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumento tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public RolUsuario getRol() { return rol; }
    public void setRol(RolUsuario rol) { this.rol = rol; }

    public EpsEntity getEps() { return eps; }
    public void setEps(EpsEntity eps) { this.eps = eps; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
