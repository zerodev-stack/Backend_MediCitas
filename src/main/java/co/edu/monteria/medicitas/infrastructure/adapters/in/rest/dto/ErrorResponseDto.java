package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import java.time.LocalDateTime;

public class ErrorResponseDto {
    private int status;
    private String error;
    private String mensaje;
    private String mensajeOrientacion;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ErrorResponseDto() {}

    public ErrorResponseDto(int status, String error, String mensaje) {
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
    }

    public ErrorResponseDto(int status, String error, String mensaje, String mensajeOrientacion) {
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
        this.mensajeOrientacion = mensajeOrientacion;
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getMensajeOrientacion() { return mensajeOrientacion; }
    public void setMensajeOrientacion(String mensajeOrientacion) { this.mensajeOrientacion = mensajeOrientacion; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
