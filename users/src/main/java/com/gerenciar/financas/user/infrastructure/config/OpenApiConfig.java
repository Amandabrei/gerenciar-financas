package com.gerenciar.financas.user.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gestão Financeira - API de Usuários")
                        .version("1.0.0")
                        .description("""
                                API responsável pelo cadastro, consulta,
                                atualização, exclusão e autenticação de usuários.

                                Também disponibiliza a importação de usuários
                                por arquivo CSV.
                                """))
                .addTagsItem(new Tag()
                        .name("Usuários")
                        .description("Cadastro e gerenciamento de usuários."))
                .addTagsItem(new Tag()
                        .name("Autenticação")
                        .description("Validação das credenciais de acesso."))
                .addTagsItem(new Tag()
                        .name("Importação CSV")
                        .description("Importação de usuários por arquivo CSV."));
    }
}
