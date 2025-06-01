# Base image con JDK
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY build/install/CharacterAI/ .

ENTRYPOINT ["bin/CharacterAI"]
