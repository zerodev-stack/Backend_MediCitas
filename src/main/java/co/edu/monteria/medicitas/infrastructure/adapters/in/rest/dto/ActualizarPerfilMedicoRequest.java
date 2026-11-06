package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos que el médico puede editar por sí mismo. Correo y teléfono son opcionales: si
 * llegan vacíos se borran.
 */
public record ActualizarPerfilMedicoRequest(
        @NotBlank(message = "La ciudad de atención es obligatoria")
        @Size(max = 80, message = "La ciudad no puede superar 80 caracteres")
        String ciudadAtencion,

        @Email(message = "Formato de correo inválido")
        @Size(max = 120, message = "El correo no puede superar 120 caracteres")
        String correoContacto,

        @Pattern(regexp = "^$|^[0-9+ ]{7,20}$", message = "El teléfono solo admite números, espacios y '+'")
        String telefonoContacto
) {}
