package com.rinconsur.gestion_visitantes.model;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "visitantes")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Entidad que representa a un visitante individual en la tienda con su acceso y rango etario")
public class Visitante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único autogenerado por la base de datos", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, length = 20)
    @Schema(description = "Código identificador del visitante (ej: VIS-001)", example = "VIS-001")
    private String codigo;

    @Column(nullable = false, length = 20)
    @Schema(description = "Fecha de ingreso", example = "2026-11-17")
    private String fecha;

    @Column(nullable = false, length = 20)
    @Schema(description = "Hora de ingreso", example = "10:30:00")
    private String hora;

    @Column(nullable = false, length = 20)
    @Schema(description = "Acceso por el cual ingresó (A1, A2, A3, A4)", example = "A1")
    private String acceso;

    @Column(nullable = false, length = 30)
    @Schema(description = "Rango etario del visitante (JOVEN, ADULTO, PERSONA_MAYOR)", example = "ADULTO")
    private String rangoEtario;

    @Column(nullable = false, length = 30)
    @Schema(description = "Estado del visitante (En_Tienda, Compro, No_Compro)", example = "En_Tienda")
    private String estado;

    @Column(length = 50, unique = true)
    @Schema(description = "Folio de boleta asociada si realizó compra", example = "12345678")
    private String folioBoleta;

    @Schema(description = "ID de la sesión activa", example = "1")
    private Long idSesion;

    @Schema(description = "ID que el visitante tiene en la base SQLite de la app Android (sirve para sincronizar)", example = "12")
    private Long idLocal;
}
