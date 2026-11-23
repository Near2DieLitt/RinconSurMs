package com.rinconsur.gestion_notificaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos requeridos para enviar un correo de clave temporal o código de recuperación")
public class CorreoRequest {

    @Schema(description = "Correo electrónico del destinatario", example = "colaborador@gmail.com")
    private String destinatario;

    @Schema(description = "Asunto del correo electrónico", example = "Rincón Sur - Clave temporal de acceso")
    private String asunto;

    @Schema(description = "Nombre del colaborador", example = "Renata Valdés")
    private String nombreColaborador;

    @Schema(description = "Clave temporal o código de verificación de 6 dígitos", example = "Rincon2026!")
    private String codigoOClave;

    @Schema(description = "Tipo de notificación (CLAVE_TEMPORAL o RECUPERACION)", example = "CLAVE_TEMPORAL")
    private String tipo;
}