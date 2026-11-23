package com.rinconsur.gestion_sesiones.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.rinconsur.gestion_sesiones.model.Sesion;
import com.rinconsur.gestion_sesiones.repository.SesionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SesionRepository sesionRep;

    @Override
    public void run(String... args) {
        if (sesionRep.count() > 0) {
            log.info("DataInitializer: sesiones ya existen, se omite carga inicial.");
            return;
        }
        Sesion s1 = new Sesion();
        s1.setCodigo("A1");
        s1.setNombre("Turno Mañana Principal");
        s1.setFecha("2026-11-17");
        s1.setActivo(true);
        s1.setCompletada(false);
        s1.setIdUsuarioAsignado(4L);
        sesionRep.save(s1);
    }
}
