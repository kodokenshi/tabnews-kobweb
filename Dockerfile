# 1. Etapa de Build (compila o projeto com Java 25)
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# Instala ferramentas necessárias para o script do Kobweb
RUN apt-get update && apt-get install -y curl unzip zip && rm -rf /var/lib/apt/lists/*

# Copia os arquivos do Gradle para aproveitar o cache
COPY gradlew .
COPY gradle gradle
COPY site/build.gradle.kts .
COPY settings.gradle.kts .
COPY site site

# Dá permissão de execução ao Gradle
RUN chmod +x gradlew

# Baixa e extrai a versão oficial do Kobweb CLI diretamente do GitHub Releases
ENV KOBWEB_VERSION=0.9.21
RUN wget https://github.com/varabyte/kobweb-cli/releases/download/v${KOBWEB_VERSION}/kobweb-${KOBWEB_VERSION}.zip \
    && unzip kobweb-${KOBWEB_VERSION}.zip -d /opt/kobweb \
    && rm kobweb-${KOBWEB_VERSION}.zip

# Adiciona o Kobweb ao PATH e executa o export
ENV PATH="/opt/kobweb/kobweb-${KOBWEB_VERSION}/bin:${PATH}"
RUN kobweb export --layout fullstack

# 2. Etapa de Execução (Imagem leve)
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copia os artefatos compilados
COPY --from=builder /app/site/.kobweb/site/system ./site/system
COPY --from=builder /app/site/.kobweb/site/server ./site/server

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-jar", "site/server/server.jar", "--env", "prod", "--port", "8080"]