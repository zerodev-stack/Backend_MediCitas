package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Credenciales de inicio de sesión. Se acepta el email o el número de documento
 * (la app móvil usa el documento); al menos uno de los dos es obligatorio.
 */
public class AuthRequest {
    @Email(message = "El formato de email no es válido")
    private String email;

    private String numDocumento;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    public AuthRequest() {}

    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    @AssertTrue(message = "El email o el número de documento es obligatorio")
    public boolean isIdentificadorPresente() {
        return (email != null && !email.isBlank()) || (numDocumento != null && !numDocumento.isBlank());
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNumDocumento() { return numDocumento; }
    public void setNumDocumento(String numDocumento) { this.numDocumento = numDocumento; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
