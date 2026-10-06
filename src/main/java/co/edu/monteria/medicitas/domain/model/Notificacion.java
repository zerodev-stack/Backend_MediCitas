package co.edu.monteria.medicitas.domain.model;

import co.edu.monteria.medicitas.domain.model.enums.TipoNotificacion;
import java.time.LocalDateTime;

public class Notificacion {
    private Long id;
    private Usuario usuario;
    private String titulo;
    private String mensaje;
    private TipoNotificacion tipo;
    private boolean leida;
    private LocalDateTime fechaEnvio;

    public Notificacion() {}

    public Notificacion(Long id, Usuario usuario, String titulo, String mensaje, TipoNotificacion tipo, boolean leida, LocalDateTime fechaEnvio) {
        this.id = id;
        this.usuario = usuario;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.tipo = tipo;
        this.leida = leida;
        this.fechaEnvio = fechaEnvio;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public TipoNotificacion getTipo() { return tipo; }
    public void setTipo(TipoNotificacion tipo) { this.tipo = tipo; }

    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }
}
