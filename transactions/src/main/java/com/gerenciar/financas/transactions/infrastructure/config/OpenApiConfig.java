
package com.gerenciar.financas.transactions.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gestão Financeira - API de Transações")
                        .version("1.0.0")
                        .description("""
                                API responsável pelo gerenciamento de transações financeiras.

                                Funcionalidades disponíveis:
                                - Registro de depósitos;
                                - Registro de saques;
                                - Registro de transferências;
                                - Registro de compras;
                                - Consulta de transações.
                                """));
    }
}
