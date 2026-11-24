package com.rinconsur.gestion_analitica.dto;

import com.rinconsur.gestion_analitica.model.RegistroFlujo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegistroFlujoRequest {
    private String acceso;
    private String fecha;
    private int ingresos;
    private int compradores;
    private int transitoJoven;
    private int transitoAdulto;
    private int transitoMayor;
    private boolean activo = true;
    private Long idLocal;

    public RegistroFlujo toEntity() {
        RegistroFlujo e = new RegistroFlujo();
        e.setAcceso(acceso);
        e.setFecha(fecha);
        e.setIngresos(ingresos);
        e.setCompradores(compradores);
        e.setTransitoJoven(transitoJoven);
        e.setTransitoAdulto(transitoAdulto);
        e.setTransitoMayor(transitoMayor);
        e.setActivo(activo);
        e.setIdLocal(idLocal);
        return e;
    }
}
