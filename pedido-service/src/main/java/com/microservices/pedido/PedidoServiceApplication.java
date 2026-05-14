package com.microservices.pedido;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PedidoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PedidoServiceApplication.class, args);
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pedido Service API")
                        .version("1.0.0")
                        .description("API REST para gestión de pedidos con comunicación inter-microservicios")
                        .contact(new Contact()
                                .name("Soporte")
                                .email("soporte@microservicios.com")));
    }
}
