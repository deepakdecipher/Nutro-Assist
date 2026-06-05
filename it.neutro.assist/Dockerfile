# Stage 1: Java code ko compile aur build karne ke liye (Maven)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Aapka pom.xml aur src folder 'it.neutro.forge' ke andar hain, isliye hum wahan se copy kar rahe hain
COPY it.neutro.forge/pom.xml .
COPY it.neutro.forge/src ./src

# Project ko compile karke JAR file generate karein
RUN mvn clean package -DskipTests

# Stage 2: Sirf JAR file ko run karne ke liye (Lightweight JRE)
FROM eclipse-temurin:21-jre
WORKDIR /app

# Stage 1 se bani hui JAR file ko safe tarike se run stage mein copy karein
COPY --from=build /app/target/*.jar app.jar

# Aapki application ka port (8081)
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
