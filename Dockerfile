# 1. Etapa de Build
FROM mcr.microsoft.com/playwright:v1.49.0-noble AS builder

# Instala o JDK
RUN apt-get update && apt-get install -y openjdk-21-jdk && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copia os arquivos do projeto
COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts .
COPY site site

RUN chmod +x gradlew

# Compila e exporta no modo FULLSTACK
RUN export KOBWEB_BUILD_TYPE=prod && ./gradlew :site:kobwebExport -Pkobweb.export.layout=FULLSTACK

# 2. Etapa de Execução (Imagem final leve)
FROM eclipse-temurin:21-jre

WORKDIR /app/site

# Copia o arquivo de configuração conf.yaml e as pastas compiladas
COPY --from=builder /app/site/.kobweb/conf.yaml ./.kobweb/conf.yaml
COPY --from=builder /app/site/.kobweb/site/system ./.kobweb/site/system
COPY --from=builder /app/site/.kobweb/server ./.kobweb/server

ENV PORT=8080
EXPOSE 8080

# Executa o servidor JVM do Kobweb
CMD ["java", "-jar", ".kobweb/server/server.jar", "--env", "prod", "--port", "8080"]