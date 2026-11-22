package com.rinconsur.gestion_usuarios.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.rinconsur.gestion_usuarios.model.Usuario;
import com.rinconsur.gestion_usuarios.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRep;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRep.count() > 0) {
            log.info("DataInitializer: usuarios ya existen, se omite la carga inicial.");
            return;
        }

        Usuario daniela = new Usuario();
        daniela.setNombre("Daniela Gonzales");
        daniela.setCorreo("rvaldes.4419@gmail.com");
        daniela.setRut("20123456-5");
        daniela.setPerfil("P-01");
        daniela.setRol("ADMIN");
        daniela.setPasswordHash(passwordEncoder.encode("Daniela123!"));
        daniela.setActivo(true);
        daniela.setDebeCambiarClave(false);
        usuarioRep.save(daniela);

        Usuario nicole = new Usuario();
        nicole.setNombre("Nicole Soto");
        nicole.setCorreo("elshield.ever.06@gmail.com");
        nicole.setRut("19876543-0");
        nicole.setPerfil("P-02");
        nicole.setRol("SUPERVISOR");
        nicole.setPasswordHash(passwordEncoder.encode("Nicole123!"));
        nicole.setActivo(true);
        nicole.setDebeCambiarClave(false);
        usuarioRep.save(nicole);

        Usuario renata = new Usuario();
        renata.setNombre("Renata Valdés");
        renata.setCorreo("revaldes007@gmail.com");
        renata.setRut("21045678-3");
        renata.setPerfil("P-03");
        renata.setRol("SUPERVISOR");
        renata.setPasswordHash(passwordEncoder.encode("Renata123!"));
        renata.setActivo(true);
        renata.setDebeCambiarClave(false);
        usuarioRep.save(renata);

        Usuario matias = new Usuario();
        matias.setNombre("Matías Olavarría");
        matias.setCorreo("mati.acevedo.05@gmail.com");
        matias.setRut("20456789-1");
        matias.setPerfil("P-04");
        matias.setRol("OBSERVADOR");
        matias.setPasswordHash(passwordEncoder.encode("Matias123!"));
        matias.setActivo(true);
        matias.setDebeCambiarClave(false);
        usuarioRep.save(matias);

        log.info("DataInitializer: 4 usuarios iniciales cargados.");
    }
}
