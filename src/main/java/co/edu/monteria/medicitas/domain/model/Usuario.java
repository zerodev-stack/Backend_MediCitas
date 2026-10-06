package co.edu.monteria.medicitas.domain.model;

import co.edu.monteria.medicitas.domain.model.enums.RolUsuario;
import co.edu.monteria.medicitas.domain.model.enums.TipoDocumento;
import java.time.LocalDateTime;

/**
 * Entidad de dominio pura (Hexagonal Domain) para Usuario.
 * Exenta de anotaciones de Spring o JPA.
 */
public class Usuario {
    private Long id;
    private String numDocumento;
    private TipoDocumento tipoDocumento;
    private String nombreCompleto;
    private String email;
    private String passwordHash;
    private RolUsuario rol;
    private Eps eps;
    private String telefono;
    private LocalDateTime fechaRegistro;

    public Usuario() {}

    public Usuario(Long id, String numDocumento, TipoDocumento tipoDocumento, String nombreCompleto,
                   String email, String passwordHash, RolUsuario rol, Eps eps, String telefono, LocalDateTime fechaRegistro) {
        this.id = id;
        this.numDocumento = numDocumento;
        this.tipoDocumento = tipoDocumento;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.eps = eps;
        this.telefono = telefono;
        this.fechaRegistro = fechaRegistro;
    }

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

    public Eps getEps() { return eps; }
    public void setEps(Eps eps) { this.eps = eps; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
