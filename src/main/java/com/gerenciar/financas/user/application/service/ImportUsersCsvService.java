package com.gerenciar.financas.user.application.service;

import com.gerenciar.financas.user.application.port.in.ImportUsersCsvFailure;
import com.gerenciar.financas.user.application.port.in.ImportUsersCsvResult;
import com.gerenciar.financas.user.application.port.in.ImportUsersCsvUseCase;
import com.gerenciar.financas.user.application.port.out.UserRepositoryPort;
import com.gerenciar.financas.user.domain.model.User;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImportUsersCsvService implements ImportUsersCsvUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    public ImportUsersCsvService(
            UserRepositoryPort userRepositoryPort,
            PasswordEncoder passwordEncoder) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ImportUsersCsvResult execute(byte[] fileContent) {

        List<ImportUsersCsvFailure> failures = new ArrayList<>();
        int successCount = 0;

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .get();

        try (Reader reader = new InputStreamReader(
                new java.io.ByteArrayInputStream(fileContent),
                StandardCharsets.UTF_8);
             CSVParser parser = format.parse(reader)) {

            if (!parser.getHeaderNames().containsAll(
                    List.of("name", "email", "password"))) {
                throw new IllegalArgumentException(
                        "O CSV deve possuir as colunas: name,email,password"
                );
            }

            for (CSVRecord record : parser) {
                int lineNumber = (int) record.getRecordNumber() + 1;

                try {
                    String name = record.get("name");
                    String email = record.get("email");
                    String password = record.get("password");

                    if (name.isBlank() || name.length() > 120) {
                        throw new IllegalArgumentException(
                                "Nome obrigatório e deve ter até 120 caracteres"
                        );
                    }

                    if (email.isBlank() || email.length() > 180
                            || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                        throw new IllegalArgumentException(
                                "E-mail inválido ou com mais de 180 caracteres"
                        );
                    }

                    if (password.isBlank() || password.length() < 8
                            || password.length() > 100) {
                        throw new IllegalArgumentException(
                                "Senha deve ter entre 8 e 100 caracteres"
                        );
                    }

                    if (userRepositoryPort.findByEmail(email).isPresent()) {
                        throw new IllegalArgumentException(
                                "E-mail já cadastrado"
                        );
                    }

                    User user = new User(
                            null,
                            name,
                            email,
                            passwordEncoder.encode(password)
                    );

                    userRepositoryPort.save(user);
                    successCount++;

                } catch (IllegalArgumentException exception) {
                    failures.add(new ImportUsersCsvFailure(
                            lineNumber,
                            exception.getMessage()
                    ));
                }
            }

        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "Não foi possível ler o arquivo CSV", exception
            );
        }

        return new ImportUsersCsvResult(
                successCount,
                failures.size(),
                failures
        );
    }
}