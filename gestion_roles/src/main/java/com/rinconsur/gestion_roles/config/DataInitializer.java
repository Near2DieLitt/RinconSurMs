package com.rinconsur.gestion_roles.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.rinconsur.gestion_roles.model.Rol;
import com.rinconsur.gestion_roles.repository.RolRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;

    @Override
    public void run(String... args) {
        if (rolRepository.count() > 0) {
            log.info("DataInitializer: roles ya existen, se omite la carga inicial.");
            return;
        }

        Rol admin = new Rol();
        admin.setCodigo("P-01");
        admin.setNombre("Administrador");
        admin.setRolSistema("ADMIN");
        admin.setPuedeGestionarUsuarios(true);
        admin.setPuedeCrearSesiones(false);
        admin.setPuedeHacerConteos(false);
        rolRepository.save(admin);

        Rol supPiso = new Rol();
        supPiso.setCodigo("P-02");
        supPiso.setNombre("Supervisor de piso");
        supPiso.setRolSistema("SUPERVISOR");
        supPiso.setPuedeGestionarUsuarios(false);
        supPiso.setPuedeCrearSesiones(true);
        supPiso.setPuedeHacerConteos(false);
        rolRepository.save(supPiso);

        Rol supCaja = new Rol();
        supCaja.setCodigo("P-03");
        supCaja.setNombre("Supervisor de caja");
        supCaja.setRolSistema("SUPERVISOR");
        supCaja.setPuedeGestionarUsuarios(false);
        supCaja.setPuedeCrearSesiones(true);
        supCaja.setPuedeHacerConteos(false);
        rolRepository.save(supCaja);

        Rol obs = new Rol();
        obs.setCodigo("P-04");
        obs.setNombre("Observador");
        obs.setRolSistema("OBSERVADOR");
        obs.setPuedeGestionarUsuarios(false);
        obs.setPuedeCrearSesiones(false);
        obs.setPuedeHacerConteos(true);
        rolRepository.save(obs);

        log.info("DataInitializer: 4 perfiles iniciales cargados.");
    }
}
