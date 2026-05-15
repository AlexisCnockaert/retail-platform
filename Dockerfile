# ---- Build stage ----

FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# On copie d'abord le pom.xml seul pour profiter du cache Docker :
# si le code change mais pas les dépendances, Maven ne les retélécharge pas
COPY pom.xml .
RUN mvn dependency:go-offline -q


COPY src ./src
RUN mvn package -DskipTests -q

# ---- Run stage ----
# Image finale légère — pas Maven, juste le JRE
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]