package com.rinconsur.gestion_usuarios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "usuarios")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Entidad que representa a un usuario o colaborador dentro del sistema Rincón Sur")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único autogenerado por la base de datos", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(nullable = false, length = 255)
    @JsonIgnore
    private String passwordHash;

    @Column(nullable = false, unique = true, length = 15)
    private String rut;

    @Column(nullable = false, length = 10)
    private String perfil;

    @Column(nullable = false, length = 30)
    private String rol;

    private boolean activo = true;

    private boolean debeCambiarClave = false;

    @Column(length = 255)
    private String descripcion;

    @Lob
    @Column(length = 16777215)
    private String avatarBase64;

    @Column(length = 10)
    @JsonIgnore
    private String codigoRecuperacion;

    @JsonIgnore
    private Long codigoExpiracionMs;

    @JsonIgnore
    private int intentosRecuperacion = 0;
}
