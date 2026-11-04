package com.gerenciar.financas.user.application.port.in;

public interface ImportUsersCsvUseCase {

    ImportUsersCsvResult execute(byte[] fileContent);
}
