package com.gerenciar.financas.transactions.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
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
                                - API responsável pelas operações financeiras
                                dos usuários autenticados.
                                - Consulta e registro das cotações de USD e EUR pela BrasilAPI;
                                - Armazenamento das taxas e datas das cotações nas transações.

                                Funcionalidades:
                                - Registro de depósitos, saques, transferências e compras;
                                - Consulta e histórico de transações;
                                - Consulta de saldo e limites;
                                - Análise de despesas por categoria, dia e mês;
                                - Exportação de relatório de transações em CSV.

                                As operações exigem autenticação HTTP Basic.
                                A documentação Swagger permanece pública.
                                """))
                .addTagsItem(new Tag()
                        .name("Transações")
                        .description("Registro e consulta de movimentações financeiras."))
                .addTagsItem(new Tag()
                        .name("Saldo")
                        .description("Consulta do saldo da conta do usuário autenticado."))
                .addTagsItem(new Tag()
                        .name("Análise de despesas")
                        .description("Consolidação das despesas aprovadas por período."))
                .addTagsItem(new Tag()
                        .name("Relatórios")
                        .description("Exportação do histórico de transações em CSV."))
                .addTagsItem(new Tag()
                        .name("Câmbio")
                        .description("Cotações de moedas estrangeiras (USD e EUR) obtidas pela BrasilAPI."))
                .components(new Components()
                        .addSecuritySchemes("basicAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("basicAuth"));
    }
}
