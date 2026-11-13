# Imagem Java de execução, configurável pelo ambiente local.
# No computador da desenvolvedora, será usada a imagem com Zscaler.
# Em outros ambientes, o padrão será o Temurin oficial.
ARG JAVA_RUNTIME_IMAGE=eclipse-temurin:25-jre

# Etapa auxiliar: disponibiliza o truststore da imagem de execução.
FROM ${JAVA_RUNTIME_IMAGE} AS runtime-base

# Etapa de compilação.
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Copia o truststore da imagem configurada para que o Maven confie
# nos certificados durante o download das dependências.
COPY --from=runtime-base /opt/java/openjdk/lib/security/cacerts /tmp/cacerts

ENV MAVEN_OPTS="-Djavax.net.ssl.trustStore=/tmp/cacerts -Djavax.net.ssl.trustStorePassword=changeit -Djavax.net.ssl.trustStoreType=PKCS12"

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY src/ src/

RUN ./mvnw clean package -DskipTests

# Etapa final: usa a imagem configurada, que já possui o truststore.
FROM runtime-base AS runtime

WORKDIR /app

# Cria o usuário apenas se ele ainda não existir na imagem-base.
RUN if ! getent group spring >/dev/null 2>&1; then addgroup --system spring; fi \
    && if ! id spring >/dev/null 2>&1; then adduser --system --ingroup spring spring; fi

COPY --from=build --chown=spring:spring /app/target/*.jar /app/app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]