# API de Gestão Financeira

Projeto desenvolvido para a BECA Bradesco Java Beginner, com mentoria NTT DATA, com o objetivo de disponibilizar uma API de gestão financeira organizada em dois microsserviços independentes, utilizando Java, Spring Boot, arquitetura hexagonal e Docker Compose.

## 1. Visão geral

A solução é composta por dois microsserviços:

| Microsserviço | Responsabilidade |
|---|---|
| **MS1 — User Service** | Cadastro, consulta, atualização, exclusão e importação de usuários por arquivo CSV. |
| **MS2 — Transaction API** | Registro e consulta de transações financeiras, validações de saldo e limites, câmbio, análise de despesas e relatórios. |

Cada microsserviço possui sua própria aplicação Spring Boot e seu próprio banco de dados PostgreSQL.

Os serviços são executados em conjunto por meio de um único `docker-compose.yml`, localizado na raiz do repositório.

## 2. Tecnologias utilizadas

- Java 25
- Spring Boot
- Spring Data JPA
- Spring Validation
- PostgreSQL
- Maven
- Docker e Docker Compose
- JUnit e Mockito
- Swagger / OpenAPI
- MockAPI — integração para consulta de informações financeiras e limites.
- BrasilAPI — integração para consulta de taxas de câmbio.

## 3. Arquitetura

Os microsserviços seguem os princípios da **Arquitetura Hexagonal (Ports and Adapters)**, separando as regras de negócio dos detalhes de infraestrutura.

A organização permite manter o domínio independente de tecnologias externas, facilitando testes, manutenção e evolução.

### MS1 — User Service

Responsável pelo gerenciamento dos usuários.

Principais funcionalidades:

- Cadastro de usuários.
- Consulta de usuários por ID.
- Listagem de usuários.
- Atualização de usuários.
- Exclusão de usuários.
- Importação de usuários por CSV.
- Validação e normalização de e-mail.
- Tratamento de erros e duplicidade de e-mail.

### MS2 — Transaction API

Responsável pelas operações financeiras.

Funcionalidades previstas no escopo:

- Registro de depósitos.
- Registro de saques.
- Registro de transferências.
- Registro de compras.
- Consulta de transações.
- Consulta de saldo e limites por integração com MockAPI.
- Consulta de taxas de câmbio pela BrasilAPI.
- Análise de despesas.
- Geração de relatório financeiro em CSV.
- Registro de transações aprovadas e rejeitadas, com justificativa para rejeições.

As transações são processadas de forma síncrona, sem utilização de filas ou mensageria no escopo obrigatório.

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
│   └── Dockerfile
│
├── docker-compose.yml
├── .env.example
├── .dockerignore
└── .gitignore
```

Cada microsserviço possui seu próprio `pom.xml`, código-fonte, testes e Dockerfile.

O arquivo `docker-compose.yml` centraliza a inicialização das aplicações e dos bancos de dados.

## 5. Pré-requisitos

Para executar o projeto, é necessário ter:

- Git.
- Docker Desktop ou Rancher Desktop.
- Docker Compose.
- Java 25, caso deseje executar os testes ou as aplicações fora dos containers.

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

Na raiz do projeto, existe o arquivo `.env.example`, contendo as variáveis necessárias para configuração dos bancos de dados e das integrações externas.

Crie uma cópia com o nome `.env`:

**Git Bash / Linux / macOS:**

```bash
cp .env.example .env
```

**Windows PowerShell:**

```powershell
Copy-Item .env.example .env
```

Edite o arquivo `.env` e substitua os valores de exemplo por senhas próprias e seguras.

As variáveis utilizadas são:

| Variável | Descrição |
|---|---|
| `USERS_POSTGRES_DB` | Nome do banco de dados do MS1 (`finances_users`). |
| `USERS_POSTGRES_USER` | Usuário do banco de dados do MS1. |
| `USERS_POSTGRES_PASSWORD` | Senha do banco de dados do MS1. |
| `TRANSACTIONS_POSTGRES_DB` | Nome do banco de dados do MS2 (`finances_transactions`). |
| `TRANSACTIONS_POSTGRES_USER` | Usuário do banco de dados do MS2. |
| `TRANSACTIONS_POSTGRES_PASSWORD` | Senha do banco de dados do MS2. |
| `USER_SERVICE_URL` | Endereço do MS1 utilizado pelo MS2. |
| `ACCOUNT_API_URL` | URL da API MockAPI utilizada para consulta de informações financeiras. |
| `EXCHANGE_API_URL` | URL da BrasilAPI utilizada para consulta de cotações. |

As URLs das integrações externas possuem valores padrão definidos na configuração da aplicação. No ambiente Docker Compose, são disponibilizadas por meio das variáveis de ambiente.

**Importante:** o arquivo `.env` contém informações locais e não deve ser enviado ao Git. O `.env.example` deve ser mantido no repositório para orientar a configuração de novos ambientes. Não armazene senhas ou credenciais reais no arquivo de exemplo.

## 7. Executando o projeto com Docker Compose

Com o Docker iniciado e o arquivo `.env` configurado, execute na raiz do repositório:

```bash
docker compose up --build -d
```

Esse comando realiza a construção das imagens e inicia os quatro containers:

| Container / Serviço | Responsabilidade | Porta local |
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

Esse comando encerra e remove os containers, mas preserva os volumes dos bancos de dados.

Para remover também os volumes e os dados persistidos, utilize:

```bash
docker compose down -v
```

**Atenção:** o último comando apaga os dados armazenados nos volumes dos bancos.

## 8. Endereços das aplicações

Com os containers em execução:

| Serviço | Endereço |
|---|---|
| MS1 — User Service | http://localhost:8082 |
| MS2 — Transaction API | http://localhost:8083 |

### Documentação Swagger / OpenAPI

A documentação interativa das APIs pode ser acessada pelos endereços:

- MS1: http://localhost:8082/swagger-ui.html
- MS2: http://localhost:8083/swagger-ui.html

Os documentos OpenAPI podem ser consultados em:

- MS1: http://localhost:8082/v3/api-docs
- MS2: http://localhost:8083/v3/api-docs

### Verificação de saúde

O MS2 disponibiliza o endpoint:

```http
GET http://localhost:8083/actuator/health
```

Uma resposta com status `UP` indica que a aplicação está em execução.

## 9. Comunicação entre os microsserviços

O MS2 utiliza o MS1 para validar informações de usuários.

Quando executados pelo Docker Compose, os serviços se comunicam pela rede interna do Docker, utilizando o nome do serviço como endereço.

A configuração utilizada é:

```text
USER_SERVICE_URL=http://user:8080
```

O endereço interno é diferente do endereço utilizado pelo computador local.

- Acesso externo ao MS1: `http://localhost:8082`
- Comunicação interna entre containers: `http://user:8080`

Essa configuração permite que o MS2 acesse o MS1 sem depender de `localhost` dentro do container.

## 10. Testes

Os testes automatizados são implementados com JUnit e Mockito.

Para executar os testes do MS1:

```bash
cd users
./mvnw test
```

No Windows, também é possível utilizar:

```cmd
mvnw.cmd test
```

Para executar os testes do MS2:

```bash
cd transactions
./mvnw test
```

No Windows:

```cmd
mvnw.cmd test
```

Os testes verificam regras de negócio, serviços, persistência, validações e tratamento de exceções.

## 11. Integrações externas

### MockAPI

Utilizada pelo MS2 para consultar informações financeiras associadas aos usuários, incluindo saldo disponível e limites de movimentação.

A indisponibilidade dessa integração não deve resultar em aprovação indevida de uma transação.

#### Consulta de saldo

O MS2 disponibiliza um endpoint para consultar o saldo atual de um usuário por meio da integração com a MockAPI.

**Endpoint:**

```http
GET /accounts/{userId}/balance
```

**Exemplo de acesso local:**

```http
GET http://localhost:8083/accounts/550e8400-e29b-41d4-a716-446655440000/balance
```

O identificador utilizado na requisição deve corresponder ao ID de um usuário existente no MS1 e com conta cadastrada na MockAPI.

**Exemplo de resposta — 200 OK:**

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "balance": 1500.00
}
```

**Possíveis respostas:**

| Status HTTP | Descrição |
|---|---|
| 200 OK | Saldo consultado com sucesso. |
| 404 Not Found | Usuário ou conta financeira não encontrada, ou saldo não disponível para o usuário informado. |
| 503 Service Unavailable | API Mock indisponível no momento. |

A consulta utiliza o identificador do usuário para localizar sua conta na MockAPI. O saldo retornado representa exclusivamente o valor disponibilizado pela integração externa.

**Importante — origem e atualização do saldo:**

O MS2 não recalcula nem atualiza automaticamente o saldo da MockAPI com base nas transações registradas em seu próprio banco de dados.

Portanto, o valor retornado representa o saldo informado pela fonte externa, e não necessariamente o saldo resultante de todas as movimentações aprovadas no MS2.

Essa separação é uma característica da implementação atual. A integração de saldo não realiza conciliação automática entre o saldo da MockAPI e as transações persistidas no MS2.

### BrasilAPI

Utilizada para consultar taxas de câmbio, permitindo apresentar valores convertidos em moedas como USD e EUR, com suas respectivas taxas e datas de referência.

A indisponibilidade da API externa não deve impedir a consulta de transações já registradas.

## 12. Segurança

O projeto considera os seguintes cuidados:

- Autenticação Basic Auth nas rotas protegidas do MS2.
- Validação dos dados recebidos pelas APIs.
- Senhas de usuários armazenadas com codificação segura.
- Não exposição de senhas em respostas ou logs.
- Separação das configurações sensíveis em variáveis de ambiente.
- Não versionamento de arquivos `.env` com credenciais reais.

### 12.1. Regras de negócio e decisões técnicas

#### Controle do limite diário

O MS2 utiliza as transações aprovadas e registradas no próprio serviço como fonte de referência para calcular os gastos diários do usuário.

A MockAPI fornece o saldo disponível, o limite diário e o limite por transação. Entretanto, o campo `dailySpent` retornado pela integração não é utilizado no cálculo, evitando a duplicidade de valores entre fontes diferentes.

Para verificar o limite diário, o MS2 considera as transações aprovadas do dia atual dos seguintes tipos:

- Saques (`WITHDRAWAL`);
- Transferências (`TRANSFER`);
- Compras (`PURCHASE`).

Depósitos e transações rejeitadas não são contabilizados como gastos.

#### Diferença entre saldo e limite diário

Embora ambos sejam utilizados nas validações financeiras, possuem fontes de referência distintas:

- **Saldo disponível:** obtido diretamente da MockAPI, sem recálculo automático pelas transações do MS2.
- **Gastos do limite diário:** calculados pelo MS2 a partir das transações aprovadas e persistidas em seu próprio banco de dados.

Dessa forma, o cálculo do limite diário considera as movimentações registradas no MS2, enquanto a consulta de saldo reflete o valor informado pela MockAPI.

#### Consulta de cotações

A integração com a BrasilAPI consulta a cotação da moeda solicitada para a data atual.

Quando não existe cotação disponível para a data consultada, como pode ocorrer em finais de semana e feriados, o MS2 realiza novas consultas retrocedendo um dia por tentativa, até encontrar uma cotação válida ou atingir o limite de dez dias de busca.

A resposta informa a taxa encontrada e a respectiva data de referência (`quoteDate`).

Falhas de comunicação ou indisponibilidade da API externa não devem impedir a consulta de transações já registradas.

## 13. Considerações finais

O projeto foi organizado em um único repositório Git, mantendo os dois microsserviços independentes e com responsabilidades separadas.

A execução centralizada pelo Docker Compose facilita a configuração do ambiente, a comunicação entre as aplicações e a reprodução do ambiente de desenvolvimento.

**Repositório:** [Amandabrei/gerenciar-financas](https://github.com/Amandabrei/gerenciar-financas)
