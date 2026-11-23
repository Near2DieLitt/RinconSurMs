package com.rinconsur.gestion_sesiones.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "sesiones")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Entidad que representa una sesión programada en un acceso de Rincón Sur")
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único autogenerado por la base de datos", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, length = 20)
    @Schema(description = "Código del acceso (A1, A2, A3, A4)", example = "A1")
    private String codigo;

    @Column(nullable = false, length = 100)
    @Schema(description = "Nombre del turno o sesión", example = "Turno Mañana")
    private String nombre;

    @Column(nullable = false, length = 20)
    @Schema(description = "Fecha de la sesión (YYYY-MM-DD)", example = "2026-11-17")
    private String fecha;

    @Schema(description = "Indica si la sesión se encuentra activa", example = "true")
    private boolean activo = true;

    @Schema(description = "Indica si el checkbox de la sesión fue marcado como completado", example = "false")
    private boolean completada = false;

    @Schema(description = "ID del colaborador asignado a la sesión", example = "4")
    private Long idUsuarioAsignado;

    @Schema(description = "ID que la sesion tiene en la base SQLite de la app Android (sirve para sincronizar)", example = "7")
    private Long idLocal;
}
