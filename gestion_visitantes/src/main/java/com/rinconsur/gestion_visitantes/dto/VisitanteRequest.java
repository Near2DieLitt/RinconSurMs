package com.rinconsur.gestion_visitantes.dto;

import com.rinconsur.gestion_visitantes.model.Visitante;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VisitanteRequest {
    private String codigo;
    private String fecha;
    private String hora;
    private String acceso;
    private String rangoEtario;
    private String estado;
    private String folioBoleta;
    private Long idSesion;
    private Long idLocal;

    public Visitante toEntity() {
        Visitante e = new Visitante();
        e.setCodigo(codigo);
        e.setFecha(fecha);
        e.setHora(hora);
        e.setAcceso(acceso);
        e.setRangoEtario(rangoEtario);
        e.setEstado(estado);
        e.setFolioBoleta(folioBoleta);
        e.setIdSesion(idSesion);
        e.setIdLocal(idLocal);
        return e;
    }
}
