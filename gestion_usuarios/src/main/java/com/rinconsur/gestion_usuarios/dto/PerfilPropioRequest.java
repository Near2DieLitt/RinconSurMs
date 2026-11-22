package com.rinconsur.gestion_usuarios.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PerfilPropioRequest {
    private String correo;
    private String descripcion;
    private String avatarBase64;
}
