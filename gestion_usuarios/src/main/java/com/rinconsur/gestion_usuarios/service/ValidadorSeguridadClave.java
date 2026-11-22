package com.rinconsur.gestion_usuarios.service;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class ValidadorSeguridadClave {

    private static final String ESPECIALES = "!@#$%&*._-+";
    private final SecureRandom random = new SecureRandom();

    public String validar5Requisitos(String clave) {
        if (clave == null || clave.length() < 8) {
            return "La contraseña debe tener al menos 8 caracteres.";
        }
        boolean mayus = false, minus = false, num = false, esp = false;
        for (char c : clave.toCharArray()) {
            if (Character.isUpperCase(c)) mayus = true;
            else if (Character.isLowerCase(c)) minus = true;
            else if (Character.isDigit(c)) num = true;
            else if (ESPECIALES.indexOf(c) >= 0) esp = true;
        }
        if (!mayus) return "Debe incluir al menos una letra mayúscula (A-Z).";
        if (!minus) return "Debe incluir al menos una letra minúscula (a-z).";
        if (!num) return "Debe incluir al menos un número (0-9).";
        if (!esp) return "Debe incluir al menos un carácter especial (!@#$%&*).";
        return null;
    }

    public String generarClaveTemporalSegura() {
        String mayus = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        String minus = "abcdefghijkmnopqrstuvwxyz";
        String nums = "23456789";
        String esps = "!@#$%&*";
        StringBuilder sb = new StringBuilder();
        sb.append(mayus.charAt(random.nextInt(mayus.length())));
        for (int i = 0; i < 4; i++) sb.append(minus.charAt(random.nextInt(minus.length())));
        for (int i = 0; i < 3; i++) sb.append(nums.charAt(random.nextInt(nums.length())));
        sb.append(esps.charAt(random.nextInt(esps.length())));
        return sb.toString();
    }
}
