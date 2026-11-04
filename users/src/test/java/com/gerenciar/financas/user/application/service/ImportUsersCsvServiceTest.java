package com.gerenciar.financas.user.application.service;

import com.gerenciar.financas.user.application.port.in.ImportUsersCsvResult;
import com.gerenciar.financas.user.application.port.out.UserRepositoryPort;
import com.gerenciar.financas.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ImportUsersCsvServiceTest {

    private UserRepositoryPort userRepositoryPort;
    private PasswordEncoder passwordEncoder;
    private ImportUserCsvService service;

    @BeforeEach
    void setUp() {
        userRepositoryPort = mock(UserRepositoryPort.class);
        passwordEncoder = mock(PasswordEncoder.class);

        service = new ImportUserCsvService(
                userRepositoryPort,
                passwordEncoder
        );
    }

    @Test
    void deveImportarUsuarioComSucesso() {
        String csv = """
                name,email,password
                Amanda,amanda@email.com,senha123
                """;

        when(userRepositoryPort.findByEmail("amanda@email.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123"))
                .thenReturn("senhaCriptografada");

        ImportUsersCsvResult result = service.execute(
                csv.getBytes(StandardCharsets.UTF_8)
        );

        assertEquals(1, result.successCount());
        assertEquals(0, result.failureCount());
        assertTrue(result.failures().isEmpty());

        verify(userRepositoryPort).save(any(User.class));
        verify(passwordEncoder).encode("senha123");
    }

    @Test
    void deveRegistrarFalhaQuandoNomeForVazio() {
        String csv = """
                name,email,password
                ,amanda@email.com,senha123
                """;

        ImportUsersCsvResult result = service.execute(
                csv.getBytes(StandardCharsets.UTF_8)
        );

        assertEquals(0, result.successCount());
        assertEquals(1, result.failureCount());
        assertEquals(2, result.failures().get(0).lineNumber());
        assertEquals(
                "Nome obrigatório e deve ter até 120 caracteres",
                result.failures().get(0).reason()
        );

        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    void deveRegistrarFalhaQuandoEmailForInvalido() {
        String csv = """
                name,email,password
                Amanda,email-invalido,senha123
                """;

        ImportUsersCsvResult result = service.execute(
                csv.getBytes(StandardCharsets.UTF_8)
        );

        assertEquals(0, result.successCount());
        assertEquals(1, result.failureCount());
        assertEquals(2, result.failures().get(0).lineNumber());

        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    void deveRegistrarFalhaQuandoSenhaForMenorQueOitoCaracteres() {
        String csv = """
                name,email,password
                Amanda,amanda@email.com,123
                """;

        ImportUsersCsvResult result = service.execute(
                csv.getBytes(StandardCharsets.UTF_8)
        );

        assertEquals(0, result.successCount());
        assertEquals(1, result.failureCount());
        assertEquals(
                "Senha deve ter entre 8 e 100 caracteres",
                result.failures().get(0).reason()
        );

        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    void deveRegistrarFalhaQuandoEmailJaEstiverCadastrado() {
        String csv = """
                name,email,password
                Amanda,amanda@email.com,senha123
                """;

        when(userRepositoryPort.findByEmail("amanda@email.com"))
                .thenReturn(Optional.of(mock(User.class)));

        ImportUsersCsvResult result = service.execute(
                csv.getBytes(StandardCharsets.UTF_8)
        );

        assertEquals(0, result.successCount());
        assertEquals(1, result.failureCount());
        assertEquals(
                "E-mail já cadastrado",
                result.failures().get(0).reason()
        );

        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    void deveLancarExcecaoQuandoCabecalhoForInvalido() {
        String csv = """
                nome,email,senha
                Amanda,amanda@email.com,senha123
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.execute(csv.getBytes(StandardCharsets.UTF_8))
        );

        assertEquals(
                "O CSV deve possuir as colunas: name,email,password",
                exception.getMessage()
        );

        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    void deveLancarExcecaoQuandoArquivoNaoPossuirCabecalho() {
        String csv = """
            Amanda,amanda@email.com,senha123
            """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.execute(csv.getBytes(StandardCharsets.UTF_8))
        );

        assertEquals(
                "O CSV deve possuir as colunas: name,email,password",
                exception.getMessage()
        );

        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    void deveRegistrarFalhaQuandoEmailSeRepetirNoArquivo() {
        String csv = """
            name,email,password
            Amanda,amanda@email.com,senha123
            Ana,AMANDA@email.com,senha456
            """;

        when(userRepositoryPort.findByEmail("amanda@email.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(anyString()))
                .thenReturn("senhaCriptografada");

        ImportUsersCsvResult result = service.execute(
                csv.getBytes(StandardCharsets.UTF_8)
        );

        assertEquals(1, result.successCount());
        assertEquals(1, result.failureCount());
        assertEquals(3, result.failures().get(0).lineNumber());
        assertEquals(
                "E-mail repetido no arquivo CSV",
                result.failures().get(0).reason()
        );

        verify(userRepositoryPort, times(1)).save(any(User.class));
        verify(userRepositoryPort, times(1)).findByEmail("amanda@email.com");
    }
}
