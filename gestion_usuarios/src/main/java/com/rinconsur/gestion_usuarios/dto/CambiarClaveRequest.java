package com.rinconsur.gestion_usuarios.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CambiarClaveRequest {
    private String correo;
    private String claveActual = "";
    private String claveNueva;
    private Boolean esPrimerInicio = false;
}
