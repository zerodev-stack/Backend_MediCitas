package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Datos de contacto editables desde "Editar datos". Nombre y documento no se incluyen
 * porque vienen de la afiliación y no se pueden cambiar.
 */
public class ActualizarPerfilRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    private String telefono;

    public ActualizarPerfilRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
