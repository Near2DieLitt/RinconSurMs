package com.rinconsur.gestion_analitica.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "registros_flujo")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Entidad que representa un registro de flujo y conversión por acceso en Rincón Sur")
public class RegistroFlujo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único autogenerado por la base de datos", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, length = 20)
    @Schema(description = "Acceso registrado (A1, A2, A3, A4)", example = "A1")
    private String acceso;

    @Column(nullable = false, length = 20)
    @Schema(description = "Fecha del registro (YYYY-MM-DD)", example = "2026-11-17")
    private String fecha;

    @Schema(description = "Total de ingresos registrados", example = "100")
    private int ingresos;

    @Schema(description = "Total de compradores con boleta", example = "40")
    private int compradores;

    @Schema(description = "Tránsito de rango Joven (<30)", example = "20")
    private int transitoJoven;

    @Schema(description = "Tránsito de rango Adulto (30-49)", example = "20")
    private int transitoAdulto;

    @Schema(description = "Tránsito de rango Persona Mayor (50+)", example = "20")
    private int transitoMayor;

    @Schema(description = "Indica si el registro se encuentra activo", example = "true")
    private boolean activo = true;

    @Schema(description = "ID del registro en la base SQLite de la app (sirve para enlazar ambos lados)", example = "15")
    private Long idLocal;
}
