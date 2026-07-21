# 1. Etapa de Build (compila o projeto com Java 25)
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# Copia os arquivos do Gradle Wrapper e as configurações da raiz
COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts .

# Copia o módulo 'site' inteiro (contém site/build.gradle.kts, site/.kobweb, site/src, etc.)
COPY site site

# Dá permissão de execução ao Gradle Wrapper
RUN chmod +x gradlew

# Compila e exporta a aplicação no layout FULLSTACK
RUN ./gradlew :site:kobwebExport -Pkobweb.export.layout=FULLSTACK

# 2. Etapa de Execução (Imagem final leve)
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copia os artefatos compilados
COPY --from=builder /app/site/.kobweb/site/system ./site/system
COPY --from=builder /app/site/.kobweb/site/server ./site/server

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-jar", "site/server/server.jar", "--env", "prod", "--port", "8080"]