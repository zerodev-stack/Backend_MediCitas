package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import co.edu.monteria.medicitas.domain.model.enums.TipoCita;
import jakarta.validation.constraints.NotNull;

public class CitaRequest {
    @NotNull(message = "El id de la disponibilidad es obligatorio")
    private Long disponibilidadId;

    private TipoCita tipo = TipoCita.CONSULTA;
    private String observaciones;

    public CitaRequest() {}

    public Long getDisponibilidadId() { return disponibilidadId; }
    public void setDisponibilidadId(Long disponibilidadId) { this.disponibilidadId = disponibilidadId; }

    public TipoCita getTipo() { return tipo; }
    public void setTipo(TipoCita tipo) { this.tipo = tipo; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
