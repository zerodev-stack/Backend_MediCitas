package co.edu.monteria.medicitas.domain.model;

import co.edu.monteria.medicitas.domain.model.enums.EstadoAutorizacion;
import java.time.LocalDateTime;

public class AutorizacionAcceso {
    private Long id;
    private HistoriaClinica historia;
    private Ips ipsSolicitante;
    private EstadoAutorizacion estado;
    private LocalDateTime fechaAutorizacion;
    private LocalDateTime fechaRevocacion;

    public AutorizacionAcceso() {}

    public AutorizacionAcceso(Long id, HistoriaClinica historia, Ips ipsSolicitante, EstadoAutorizacion estado, LocalDateTime fechaAutorizacion, LocalDateTime fechaRevocacion) {
        this.id = id;
        this.historia = historia;
        this.ipsSolicitante = ipsSolicitante;
        this.estado = estado;
        this.fechaAutorizacion = fechaAutorizacion;
        this.fechaRevocacion = fechaRevocacion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public HistoriaClinica getHistoria() { return historia; }
    public void setHistoria(HistoriaClinica historia) { this.historia = historia; }

    public Ips getIpsSolicitante() { return ipsSolicitante; }
    public void setIpsSolicitante(Ips ipsSolicitante) { this.ipsSolicitante = ipsSolicitante; }

    public EstadoAutorizacion getEstado() { return estado; }
    public void setEstado(EstadoAutorizacion estado) { this.estado = estado; }

    public LocalDateTime getFechaAutorizacion() { return fechaAutorizacion; }
    public void setFechaAutorizacion(LocalDateTime fechaAutorizacion) { this.fechaAutorizacion = fechaAutorizacion; }

    public LocalDateTime getFechaRevocacion() { return fechaRevocacion; }
    public void setFechaRevocacion(LocalDateTime fechaRevocacion) { this.fechaRevocacion = fechaRevocacion; }
}
