package com.rinconsur.gestion_sesiones.dto;

import com.rinconsur.gestion_sesiones.model.Sesion;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SesionRequest {
    private String codigo;
    private String nombre;
    private String fecha;
    private Long idUsuarioAsignado;
    private Long idLocal;

    public Sesion toEntity() {
        Sesion e = new Sesion();
        e.setCodigo(codigo);
        e.setNombre(nombre);
        e.setFecha(fecha);
        e.setIdUsuarioAsignado(idUsuarioAsignado);
        e.setIdLocal(idLocal);
        return e;
    }
}
