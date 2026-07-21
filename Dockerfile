# 1. Etapa de Build (compila o projeto com Java 25)
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# Copia os arquivos do Gradle para aproveitar o cache
COPY gradlew .
COPY gradle gradle
COPY site/build.gradle.kts .
COPY settings.gradle.kts .
COPY site site

# Dá permissão de execução e instala o Kobweb CLI
RUN chmod +x gradlew
RUN apt-get update && apt-get install -y curl unzip
RUN curl -s https://raw.githubusercontent.com/varabyte/kobweb/main/cli/install.sh | bash

# Exporta/Builda a aplicação Fullstack do Kobweb
ENV PATH="/root/.kobweb/bin:${PATH}"
RUN kobweb export --layout fullstack

# 2. Etapa de Execução (imagem final leve)
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copia os arquivos compilados do estágio anterior
COPY --from=builder /app/site/.kobweb/site/system ./site/system
COPY --from=builder /app/site/.kobweb/site/server ./site/server

# O Render injeta a porta dinamicamente através da variável PORT
ENV PORT=8080
EXPOSE 8080

# Executa o servidor JVM do Kobweb
CMD ["java", "-jar", "site/server/server.jar", "--env", "prod", "--port", "8080"]