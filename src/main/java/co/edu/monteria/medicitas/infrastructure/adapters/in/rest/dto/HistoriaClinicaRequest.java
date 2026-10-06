package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Petición para registrar una atención clínica.
 *
 * NO incluye la IPS de origen: esa se deriva del médico autenticado. Aceptarla desde el
 * exterior permitiría dejar un registro bajo la custodia de una institución que en realidad
 * no registró la atención.
 */
public class HistoriaClinicaRequest {

    @NotNull(message = "El id del paciente es obligatorio")
    private Long pacienteId;

    @NotBlank(message = "El diagnóstico es obligatorio")
    private String diagnostico;

    private String tratamiento;

    @NotBlank(message = "El contenido clínico es obligatorio")
    private String contenido;

    public HistoriaClinicaRequest() {}

    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
}
