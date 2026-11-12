package com.gerenciar.financas.user.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            UserAuthenticationProvider authenticationProvider
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).hasAnyRole("USER", "SERVICE")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/users",
                                "/users/authenticate"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/users"
                        ).hasRole("SERVICE")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/users/*"
                        ).hasAnyRole("USER", "SERVICE")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/users/*"
                        ).hasRole("USER")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/users/*"
                        ).hasRole("USER")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/users/import"
                        ).hasAnyRole("SERVICE")

                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
