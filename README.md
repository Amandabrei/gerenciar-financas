# API de Gestão Financeira

Projeto desenvolvido para a **BECA Bradesco Java Beginner**, com mentoria NTT DATA, com o objetivo de disponibilizar uma API de gestão financeira composta por dois microsserviços independentes, utilizando Java, Spring Boot, Arquitetura Hexagonal e Docker Compose.

## 1. Visão geral

A solução é composta por dois microsserviços:

| Microsserviço | Responsabilidade |
|---|---|
| **MS1 — User Service** | Cadastro, consulta, atualização, exclusão e importação de usuários por arquivo CSV. |
| **MS2 — Transaction API** | Registro e consulta de transações financeiras, validação de saldo e limites, consulta de câmbio, análise de despesas e geração de relatórios. |

Cada microsserviço possui sua própria aplicação Spring Boot e seu próprio banco de dados PostgreSQL.

Os serviços são executados em conjunto por meio de um único arquivo `docker-compose.yml`, localizado na raiz do repositório.

## 2. Tecnologias utilizadas

- Java 25
- Spring Boot
- Spring Data JPA
- Spring Validation
- Spring Security
- PostgreSQL
- Maven
- Docker e Docker Compose
- JUnit e Mockito
- JaCoCo
- Swagger / OpenAPI
- Flyway
- MockAPI
- BrasilAPI

## 3. Arquitetura

Os microsserviços seguem os princípios da **Arquitetura Hexagonal (Ports and Adapters)**, separando as regras de negócio dos detalhes de infraestrutura.

A estrutura organiza a aplicação em camadas com responsabilidades distintas:

- **Core / Domain:** modelos e regras de negócio independentes de frameworks.
- **Application:** casos de uso e orquestração das operações.
- **Ports:** contratos de entrada e saída da aplicação.
- **Adapters:** integração com HTTP, banco de dados e serviços externos.

Essa organização favorece a manutenção, a testabilidade e a evolução independente dos microsserviços.

### MS1 — User Service

Responsável pelo gerenciamento dos usuários.

Principais funcionalidades:

- Cadastro de usuários.
- Consulta de usuário por ID.
- Listagem de usuários.
- Atualização de dados cadastrais.
- Exclusão de usuários.
- Importação de usuários por arquivo CSV.
- Validação e normalização de e-mail.
- Tratamento de e-mails duplicados.
- Validação dos dados recebidos.
- Documentação da API com Swagger / OpenAPI.

### MS2 — Transaction API

Responsável pelo gerenciamento das operações financeiras.

Principais funcionalidades:

- Registro de depósitos.
- Registro de saques.
- Registro de transferências entre usuários.
- Registro de compras.
- Consulta de transação por ID.
- Listagem de transações por usuário.
- Filtros por período e tipo de transação.
- Registro de transações aprovadas e rejeitadas.
- Justificativa para transações rejeitadas.
- Consulta de saldo e limites por integração com MockAPI.
- Validação de saldo disponível e limites de movimentação.
- Consulta de taxas de câmbio pela BrasilAPI.
- Registro dos snapshots de saldo anterior e posterior às operações, quando disponíveis.
- Análise de despesas.
- Geração de relatório financeiro em CSV.
- Proteção das rotas por autenticação Basic Auth.
- Documentação da API com Swagger / OpenAPI.

As transações são processadas de forma síncrona, sem utilização de filas ou mensageria no escopo obrigatório.

Transações rejeitadas permanecem registradas para manter o histórico das tentativas de movimentação. Não existe exclusão de transações financeiras; eventuais correções devem ser realizadas por meio de novas operações.

## 4. Estrutura do repositório

```text
gerenciar-financas/
│
├── users/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── README.md
│
├── transactions/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   ├── .dockerignore
│   └── README.md
│
├── docker-compose.yml
├── .env.example
├── .dockerignore
└── .gitignore
```

Cada microsserviço possui seu próprio código-fonte, arquivo Maven, testes e Dockerfile.

O arquivo `docker-compose.yml` centraliza a inicialização das aplicações e dos bancos de dados.

## 5. Pré-requisitos

Para executar o projeto, é necessário ter:

- Git.
- Docker Desktop ou Rancher Desktop.
- Docker Compose.
- Java 25, caso deseje executar as aplicações ou os testes fora dos containers.

O Docker deve estar iniciado antes da execução dos comandos.

## 6. Configuração do ambiente

### 6.1. Clonar o repositório

```bash
git clone https://github.com/Amandabrei/gerenciar-financas.git
```

Acessar a pasta do projeto:

```bash
cd gerenciar-financas
```

### 6.2. Configurar as variáveis de ambiente

O repositório disponibiliza o arquivo `.env.example` com as variáveis necessárias para configuração dos bancos de dados e das integrações externas.

Crie uma cópia com o nome `.env`.

**Git Bash / Linux / macOS:**

```bash
cp .env.example .env
```

**Windows PowerShell:**

```powershell
Copy-Item .env.example .env
```

Edite o arquivo `.env` e substitua os valores de exemplo por credenciais próprias e seguras.

Variáveis utilizadas:

| Variável | Descrição |
|---|---|
| `USERS_POSTGRES_DB` | Nome do banco de dados do MS1. |
| `USERS_POSTGRES_USER` | Usuário do banco de dados do MS1. |
| `USERS_POSTGRES_PASSWORD` | Senha do banco de dados do MS1. |
| `TRANSACTIONS_POSTGRES_DB` | Nome do banco de dados do MS2. |
| `TRANSACTIONS_POSTGRES_USER` | Usuário do banco de dados do MS2. |
| `TRANSACTIONS_POSTGRES_PASSWORD` | Senha do banco de dados do MS2. |
| `USER_SERVICE_CLIENT_USERNAME` | Usuário utilizado na comunicação autenticada entre os microsserviços. |
| `USER_SERVICE_CLIENT_PASSWORD` | Senha utilizada na comunicação autenticada entre os microsserviços. |
| `ACCOUNT_API_URL` | Endereço da API MockAPI utilizada para consulta de saldo e limites. |
| `EXCHANGE_API_URL` | Endereço da BrasilAPI utilizada para consulta de cotações. |

O endereço interno do MS1 utilizado pelo MS2 é configurado pelo Docker Compose como `http://user:8080`.

**Importante:** o arquivo `.env` contém informações locais e não deve ser enviado ao Git. Mantenha o `.env.example` atualizado, utilizando apenas valores ilustrativos, sem credenciais reais.

## 7. Executando o projeto com Docker Compose

Com o Docker iniciado e o arquivo `.env` configurado, execute na raiz do repositório:

```bash
docker compose up --build -d
```

Esse comando constrói as imagens e inicia os quatro serviços:

| Serviço | Responsabilidade | Porta local |
|---|---|---:|
| `postgres-users` | Banco de dados do MS1 | 5434 |
| `postgres-transactions` | Banco de dados do MS2 | 5435 |
| `user` | API de usuários | 8082 |
| `transactions` | API de transações | 8083 |

Cada microsserviço utiliza seu próprio banco PostgreSQL, mantendo a separação dos dados.

### 7.1. Verificar os containers

```bash
docker compose ps
```

Os bancos de dados devem apresentar o estado `healthy`, e as aplicações devem permanecer em execução.

### 7.2. Consultar os logs

Para acompanhar todos os serviços:

```bash
docker compose logs -f
```

Para acompanhar somente as aplicações:

```bash
docker compose logs -f user transactions
```

### 7.3. Encerrar a execução

```bash
docker compose down
```

Esse comando encerra e remove os containers, preservando os volumes dos bancos de dados.

Para remover também os volumes e os dados persistidos:

```bash
docker compose down -v
```

**Atenção:** esse último comando apaga os dados armazenados nos volumes dos bancos.

## 8. Endereços das aplicações

Com os containers em execução:

| Serviço | Endereço |
|---|---|
| MS1 — User Service | http://localhost:8082 |
| MS2 — Transaction API | http://localhost:8083 |

### 8.1. Swagger / OpenAPI

A documentação interativa das APIs está disponível em:

- MS1: http://localhost:8082/swagger-ui.html
- MS2: http://localhost:8083/swagger-ui.html

Os documentos OpenAPI podem ser consultados em:

- MS1: http://localhost:8082/v3/api-docs
- MS2: http://localhost:8083/v3/api-docs

O Swagger permite consultar os endpoints, visualizar os parâmetros, verificar os modelos de requisição e resposta e executar operações diretamente pela interface.

### 8.2. Verificação de saúde

O MS2 disponibiliza o endpoint:

```http
GET http://localhost:8083/actuator/health
```

Uma resposta com status `UP` indica que a aplicação está em execução.

## 9. Comunicação entre os microsserviços

O MS2 utiliza o MS1 para validar informações dos usuários envolvidos nas operações financeiras.

Quando executados pelo Docker Compose, os serviços se comunicam pela rede interna do Docker, utilizando o nome do serviço como endereço.

Configuração interna:

```text
USER_SERVICE_URL=http://user:8080
```

Os endereços externos e internos são diferentes:

- Acesso externo ao MS1: `http://localhost:8082`.
- Comunicação interna entre containers: `http://user:8080`.

Essa configuração permite que o MS2 acesse o MS1 sem depender de `localhost` dentro do container.

A comunicação interna utiliza credenciais configuradas por variáveis de ambiente.

## 10. Segurança

O projeto implementa mecanismos de segurança para proteger as operações financeiras e os dados dos usuários.

Principais cuidados:

- Autenticação Basic Auth nas rotas protegidas do MS2.
- Comunicação autenticada entre MS1 e MS2.
- Validação dos dados recebidos pelas APIs.
- Senhas de usuários armazenadas com codificação segura.
- Não exposição de senhas em respostas ou logs.
- Separação de configurações sensíveis em variáveis de ambiente.
- Não versionamento de arquivos `.env` com credenciais reais.

As rotas públicas e protegidas devem ser consultadas na documentação Swagger de cada microsserviço.

## 11. Integrações externas

### 11.1. MockAPI — saldo e limites

O MS2 utiliza a MockAPI para consultar informações financeiras associadas aos usuários.

A integração disponibiliza informações como:

- Saldo disponível.
- Limite diário.
- Limite por transação.

O MS2 utiliza essas informações para validar operações financeiras.

A consulta de saldo é disponibilizada pelo endpoint:

```http
GET /accounts/{userId}/balance
```

Exemplo de acesso local:

```http
GET http://localhost:8083/accounts/{userId}/balance
```

O identificador informado deve corresponder a um usuário existente no MS1 e a uma conta cadastrada na MockAPI.

**Exemplo ilustrativo de resposta:**

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "balance": 1500.00
}
```

A indisponibilidade da MockAPI não deve resultar em aprovação indevida de uma transação.

#### Regras de validação de saldo e limites

As operações de saque, transferência e compra passam pelas validações de saldo e limites.

Depósitos não dependem da validação de saldo disponível para serem registrados.

O MS2 considera as transações aprovadas e persistidas em seu próprio banco para calcular os gastos diários.

São contabilizados como gastos:

- Saques (`WITHDRAWAL`).
- Transferências (`TRANSFER`).
- Compras (`PURCHASE`).

Depósitos e transações rejeitadas não são contabilizados como gastos.

O campo `dailySpent` disponibilizado pela MockAPI não é utilizado como fonte de cálculo do gasto diário, evitando duplicidade com o histórico de transações mantido pelo MS2.

#### Origem do saldo

O saldo retornado pela consulta representa o valor informado pela MockAPI.

O MS2 não realiza conciliação automática entre o saldo da MockAPI e as transações persistidas em seu próprio banco.

Essa distinção é importante: o saldo externo e o histórico de movimentações do MS2 possuem fontes de referência diferentes.

#### Snapshots de saldo

Quando as informações necessárias estão disponíveis, o MS2 registra snapshots do saldo anterior e posterior à operação.

Esses valores permitem acompanhar o contexto financeiro associado à transação registrada.

Quando não é possível determinar o saldo com segurança, os snapshots podem permanecer nulos.

### 11.2. BrasilAPI — cotação de moedas

O MS2 utiliza a BrasilAPI para consultar taxas de câmbio e disponibilizar valores convertidos em moedas como USD e EUR.

A resposta apresenta a taxa de conversão e a respectiva data de referência (`quoteDate`).

#### Tratamento de datas sem cotação

Quando não existe cotação disponível para a data consultada, como pode ocorrer em finais de semana e feriados, o MS2 realiza novas consultas retrocedendo um dia por tentativa.

A busca é limitada a dez dias.

A integração trata respostas inválidas e falhas de comunicação sem impedir a consulta de transações já registradas.

## 12. Análise de despesas e relatório CSV

O MS2 disponibiliza recursos para acompanhar as despesas dos usuários.

### 12.1. Análise de despesas

A análise considera somente transações de saída aprovadas:

- Saques.
- Transferências.
- Compras.

Transações rejeitadas e depósitos não entram no cálculo das despesas.

Quando não é informado um período, a análise utiliza o mês atual como referência.

### 12.2. Relatório financeiro

O MS2 disponibiliza a geração de relatório financeiro em CSV para o usuário autenticado.

O arquivo é preparado para facilitar a abertura e análise em ferramentas de planilhas, como Microsoft Excel.

A documentação Swagger apresenta os parâmetros e detalhes da operação.

## 13. Testes automatizados e cobertura

Os microsserviços possuem testes automatizados utilizando JUnit e Mockito.

Os testes verificam regras de negócio, serviços, validações, persistência e tratamento de exceções.

### 13.1. Executar os testes do MS1

```bash
cd users
./mvnw test
```

No Windows:

```cmd
mvnw.cmd test
```

### 13.2. Executar os testes do MS2

```bash
cd transactions
./mvnw clean verify
```

No Windows:

```cmd
mvnw.cmd clean verify
```

O comando `verify` executa os testes e gera o relatório de cobertura configurado pelo JaCoCo.

Após a execução, o relatório HTML pode ser consultado em:

```text
transactions/target/site/jacoco/index.html
```

O relatório apresenta métricas de cobertura do código, permitindo identificar quais partes da aplicação foram exercitadas pelos testes automatizados.

## 14. Decisões técnicas e regras de negócio

### Processamento síncrono

As operações financeiras são processadas de forma síncrona. Não são utilizadas filas ou mensageria no escopo obrigatório.

### Separação dos bancos de dados

O MS1 e o MS2 possuem bancos PostgreSQL independentes.

O MS2 consulta informações de usuários por meio da API do MS1, sem compartilhar tabelas ou acessar diretamente o banco de dados do outro serviço.

### Histórico de transações

As transações aprovadas e rejeitadas são persistidas.

Não existe exclusão de transações financeiras. Correções devem ser representadas por novas operações, preservando o histórico.

### Indisponibilidade de serviços externos

A indisponibilidade da MockAPI não deve permitir aprovações sem validação de saldo e limites.

A indisponibilidade da BrasilAPI não deve impedir a consulta de transações já registradas.

### Evolução do banco de dados

O MS2 utiliza migrations para controlar alterações estruturais do banco de dados.

As migrations permitem registrar a evolução do esquema de persistência, incluindo os campos utilizados para armazenar os snapshots de saldo das transações.

## 15. Considerações finais

O projeto foi desenvolvido em um único repositório Git, mantendo os dois microsserviços independentes e com responsabilidades bem definidas.

A arquitetura hexagonal favorece a separação das regras de negócio, enquanto o Docker Compose simplifica a inicialização e a comunicação entre as aplicações.

A solução reúne funcionalidades de gerenciamento de usuários, operações financeiras, validações de saldo e limites, consulta de câmbio, análise de despesas e geração de relatórios.

**Repositório:** https://github.com/Amandabrei/gerenciar-financas
