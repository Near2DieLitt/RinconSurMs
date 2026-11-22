package com.rinconsur.gestion_usuarios.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ActualizarDatosRequest {
    private String nombre = "";
    private String correo = "";
}
