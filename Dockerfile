# 1. Etapa de Build (compila o projeto com Java 25)
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# Copia a estrutura do projeto
COPY gradlew .
COPY gradle gradle
COPY site/build.gradle.kts .
COPY settings.gradle.kts .
COPY site site

# Dá permissão de execução ao Gradle
RUN chmod +x gradlew

# Compila e exporta a aplicação no layout fullstack direto pelo Gradle
RUN ./gradlew :site:kobwebExport -Pkobweb.export.layout=FULLSTACK

# 2. Etapa de Execução (Imagem final leve)
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copia os arquivos compilados gerados pelo Gradle
COPY --from=builder /app/site/.kobweb/site/system ./site/system
COPY --from=builder /app/site/.kobweb/site/server ./site/server

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-jar", "site/server/server.jar", "--env", "prod", "--port", "8080"]