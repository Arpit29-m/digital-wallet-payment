package com.digitalwallet.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger / OpenAPI 3 setup.
 *
 * The @SecurityScheme annotation at the top wires up the "Authorize" button
 * in Swagger UI so testers can paste in a JWT and hit secured endpoints
 * without needing Postman.
 */
@Configuration
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Paste your JWT token here. You can get one from POST /api/auth/login"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI digitalWalletOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Digital Wallet & P2P Payment API")
                .version("1.0.0")
                .description("""
                    REST API for managing digital wallets and peer-to-peer money transfers.

                    **Authentication**: Use `POST /auth/login` to get a JWT, then click
                    **Authorize** and paste the token.
                    """)
                .contact(new Contact()
                    .name("Wallet API Team")
                    .email("api@digitalwallet.com"))
                .license(new License()
                    .name("MIT")
                    .url("https://opensource.org/licenses/MIT"))
            )
            .servers(List.of(
                new Server().url("http://localhost:8080/api").description("Local dev"),
                new Server().url("https://api.digitalwallet.com").description("Production")
            ));
    }
}
