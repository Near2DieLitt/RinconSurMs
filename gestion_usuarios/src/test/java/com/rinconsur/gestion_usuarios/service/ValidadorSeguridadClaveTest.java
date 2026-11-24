package com.rinconsur.gestion_usuarios.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test Unit de ValidadorSeguridadClave")
public class ValidadorSeguridadClaveTest {

    private final ValidadorSeguridadClave validador = new ValidadorSeguridadClave();

    @Test
    @DisplayName("validar5Requisitos() rechaza claves invalidas y acepta claves seguras")
    void validar5Requisitos_cubreTodasLasReglas() {
        assertNotNull(validador.validar5Requisitos(null));
        assertNotNull(validador.validar5Requisitos("Ab1!"));
        assertNotNull(validador.validar5Requisitos("sinmayus1!"));
        assertNotNull(validador.validar5Requisitos("SINMINUS1!"));
        assertNotNull(validador.validar5Requisitos("SinNumero!"));
        assertNotNull(validador.validar5Requisitos("SinEspecial1 "));
        assertNull(validador.validar5Requisitos("RinconSur2026!"));
    }

    @Test
    @DisplayName("generarClaveTemporalSegura() genera una clave que cumple los 5 requisitos")
    void generarClaveTemporalSegura_cumpleRequisitos() {
        String clave = validador.generarClaveTemporalSegura();
        assertNotNull(clave);
        assertNull(validador.validar5Requisitos(clave));
    }
}
