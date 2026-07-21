# 1. Etapa de Build (compila o projeto com Java 25)
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# Instala ferramentas essenciais LOGO NO INÍCIO
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Baixa e extrai o Kobweb CLI diretamente
ENV KOBWEB_VERSION=0.9.21
RUN curl -sSL -O "https://github.com/varabyte/kobweb-cli/releases/download/v${KOBWEB_VERSION}/kobweb-${KOBWEB_VERSION}.zip" \
    && unzip "kobweb-${KOBWEB_VERSION}.zip" -d /opt/kobweb \
    && rm "kobweb-${KOBWEB_VERSION}.zip"

# Copia a estrutura do projeto
COPY gradlew .
COPY gradle gradle
COPY site/build.gradle.kts .
COPY settings.gradle.kts .
COPY site site

# Dá permissão de execução ao Gradle
RUN chmod +x gradlew

# Adiciona o Kobweb CLI ao PATH e exporta a aplicação Fullstack
ENV PATH="/opt/kobweb/kobweb-${KOBWEB_VERSION}/bin:${PATH}"
RUN kobweb export --layout fullstack

# 2. Etapa de Execução (Imagem final leve)
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copia os arquivos compilados da etapa de build
COPY --from=builder /app/site/.kobweb/site/system ./site/system
COPY --from=builder /app/site/.kobweb/site/server ./site/server

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-jar", "site/server/server.jar", "--env", "prod", "--port", "8080"]