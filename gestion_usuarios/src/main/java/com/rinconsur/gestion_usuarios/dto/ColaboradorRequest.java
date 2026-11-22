package com.rinconsur.gestion_usuarios.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ColaboradorRequest {
    private String nombre = "";
    private String correo = "";
    private String rut = "";
    private String perfil = "P-04";
    private String rol = "OBSERVADOR";
}
