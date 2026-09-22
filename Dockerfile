FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew :site:assemble --no-daemon

COPY --from=builder /app /app

EXPOSE 8080

CMD ["./gradlew", ":site:kobwebStart", "-PkobwebEnv=PROD", "-PkobwebRunLayout=FULLSTACK", "-Pserver.host=0.0.0.0", "--no-daemon"]