# 1. Etapa de Build: Usa a imagem oficial do Playwright (já vem com Chromium e dependências do Linux)
FROM mcr.microsoft.com/playwright:v1.49.0-noble AS builder

# Instala o Java 25 (ou OpenJDK) sobre a imagem que já tem o Chromium pronto
RUN apt-get update && apt-get install -y openjdk-25-jdk && rm -rf /var/lib/apt/lists/*

WORKDIR /

# Copia os arquivos do projeto
COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts .
COPY site site

RUN chmod +x gradlew

# Compila e exporta
RUN ./gradlew :site:kobwebExport -Pkobweb.export.layout=FULLSTACK

# 2. Etapa de Execução (Imagem final super leve)
FROM eclipse-temurin:25-jre

WORKDIR /

COPY --from=builder /site/.kobweb/site/system ./site/system
COPY --from=builder /site/.kobweb/site/server ./site/server

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-jar", "site/server/server.jar", "--env", "prod", "--port", "8080"]