package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Publicación de un bloque. 'fechas' admite varios días: la app los calcula a partir de
 * los días específicos marcados en el calendario o de los días de la semana elegidos.
 */
public record BloqueRequest(
        @NotNull(message = "El servicio es obligatorio") Long servicioId,
        @NotNull(message = "La sede es obligatoria") Long sedeId,
        @NotEmpty(message = "Selecciona al menos un día") List<@NotNull LocalDate> fechas,
        @NotNull(message = "La hora de inicio es obligatoria") LocalTime horaInicio,
        @NotNull(message = "La hora de fin es obligatoria") LocalTime horaFin,
        @NotNull(message = "La duración de cada cupo es obligatoria")
        @Min(value = 5, message = "La duración mínima de un cupo es 5 minutos")
        @Max(value = 240, message = "La duración máxima de un cupo es 240 minutos")
        Integer duracionCupoMinutos
) {}
