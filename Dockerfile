# 1. Etapa de Build
FROM mcr.microsoft.com/playwright:v1.49.0-noble AS builder

# Instala o JDK
RUN apt-get update && apt-get install -y openjdk-25-jdk && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copia os arquivos do projeto
COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts .
COPY site site

RUN chmod +x gradlew

# Compila e exporta no modo FULLSTACK
RUN ./gradlew :site:kobwebExport -Pkobweb.export.layout=FULLSTACK

# 2. Etapa de Execução (Imagem final leve)
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copia os arquivos estáticos e o jar do servidor a partir dos caminhos corretos do Kobweb
COPY --from=builder /app/site/.kobweb/site/system ./site/system
COPY --from=builder /app/site/.kobweb/server ./site/server

ENV PORT=8080
EXPOSE 8080

# Executa o servidor JVM do Kobweb
CMD ["java", "-jar", "site/server/server.jar", "--env", "prod", "--port", "8080"]