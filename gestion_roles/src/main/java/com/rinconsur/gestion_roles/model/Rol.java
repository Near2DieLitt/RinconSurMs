package com.rinconsur.gestion_roles.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "roles")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Entidad que representa los perfiles y roles de acceso asignados a los usuarios")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del rol, autogenerado por la base de datos", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    @Schema(description = "Código único del perfil", example = "P-01")
    private String codigo;

    @Column(nullable = false, length = 80)
    @Schema(description = "Nombre descriptivo del perfil", example = "Administrador")
    private String nombre;

    @Column(nullable = false, length = 30)
    @Schema(description = "Rol interno del sistema", example = "ADMIN")
    private String rolSistema;

    @Schema(description = "Indica si el perfil puede crear y modificar usuarios en Equipo", example = "true")
    private boolean puedeGestionarUsuarios;

    @Schema(description = "Indica si el perfil puede programar sesiones", example = "false")
    private boolean puedeCrearSesiones;

    @Schema(description = "Indica si el perfil puede realizar conteos de visitantes", example = "false")
    private boolean puedeHacerConteos;
}
