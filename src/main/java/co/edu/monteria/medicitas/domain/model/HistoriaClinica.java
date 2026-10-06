package co.edu.monteria.medicitas.domain.model;

import java.time.LocalDateTime;

public class HistoriaClinica {
    private Long id;
    private Usuario paciente;
    private Ips ipsOrigen;
    private String diagnostico;
    private String tratamiento;
    private String contenido;
    private LocalDateTime fechaRegistro;

    public HistoriaClinica() {}

    public HistoriaClinica(Long id, Usuario paciente, Ips ipsOrigen, String diagnostico, String tratamiento, String contenido, LocalDateTime fechaRegistro) {
        this.id = id;
        this.paciente = paciente;
        this.ipsOrigen = ipsOrigen;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.contenido = contenido;
        this.fechaRegistro = fechaRegistro;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getPaciente() { return paciente; }
    public void setPaciente(Usuario paciente) { this.paciente = paciente; }

    public Ips getIpsOrigen() { return ipsOrigen; }
    public void setIpsOrigen(Ips ipsOrigen) { this.ipsOrigen = ipsOrigen; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
