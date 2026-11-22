package com.rinconsur.gestion_usuarios.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RecuperacionRequest {
    private String correo;
    private String codigo;
    private String nuevaClave;
}
