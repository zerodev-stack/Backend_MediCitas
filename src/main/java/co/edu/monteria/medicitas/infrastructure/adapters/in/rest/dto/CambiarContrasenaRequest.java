package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

/** Petición de "Cambiar contraseña": la actual (para verificar) y la nueva. */
public class CambiarContrasenaRequest {

    @NotBlank(message = "La contraseña actual es obligatoria")
    private String actual;

    @NotBlank(message = "La contraseña nueva es obligatoria")
    private String nueva;

    public CambiarContrasenaRequest() {}

    public String getActual() { return actual; }
    public void setActual(String actual) { this.actual = actual; }

    public String getNueva() { return nueva; }
    public void setNueva(String nueva) { this.nueva = nueva; }
}
