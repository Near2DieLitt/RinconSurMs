package com.rinconsur.gestion_notificaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Respuesta del envío de correo electrónico")
public class CorreoResponse {

    @Schema(description = "Indica si el correo fue enviado correctamente", example = "true")
    private boolean enviado;

    @Schema(description = "Mensaje descriptivo del resultado", example = "Se ha enviado un mensaje al propietario del correo")
    private String mensaje;
}

