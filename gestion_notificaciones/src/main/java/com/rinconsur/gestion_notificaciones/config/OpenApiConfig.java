package com.rinconsur.gestion_notificaciones.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ms-notificaciones API")
                        .version("1.0.0")
                        .description("API REST para el envío de claves temporales y códigos por correo Gmail")
                        .contact(new Contact()
                                .name("Rincón Sur")
                                .email("contacto@rinconsur.cl")))
                .addServersItem(new Server()
                        .url("http://localhost:8083")
                        .description("Servidor de desarrollo local"));
    }
}
