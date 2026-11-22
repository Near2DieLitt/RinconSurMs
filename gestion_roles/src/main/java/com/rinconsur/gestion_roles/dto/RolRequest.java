package com.rinconsur.gestion_roles.dto;


import com.rinconsur.gestion_roles.model.Rol;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RolRequest {
    private String codigo;
    private String nombre;
    private String rolSistema;
    private boolean puedeGestionarUsuarios;
    private boolean puedeCrearSesiones;
    private boolean puedeHacerConteos;

    public Rol toEntity() {
        Rol e = new Rol();
        e.setCodigo(codigo);
        e.setNombre(nombre);
        e.setRolSistema(rolSistema);
        e.setPuedeGestionarUsuarios(puedeGestionarUsuarios);
        e.setPuedeCrearSesiones(puedeCrearSesiones);
        e.setPuedeHacerConteos(puedeHacerConteos);
        return e;
    }
}
