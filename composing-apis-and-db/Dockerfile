# Use the official maven/Java 8 image to create a build artifact.
# This is based on Debian and sets up Maven and Java for us.
FROM maven:3.9.4-eclipse-temurin-17 AS build

WORKDIR /app

RUN ls /opt/m2/repository
# .flattended-pom.xml is created by running start.sh
COPY target/.flattened-pom.xml ./pom.xml
COPY services services/
COPY taxi taxi/

# Build a release artifact.
RUN mvn package -DskipTests -Dmaven.repo.local=/opt/m2/repository

FROM eclipse-temurin:17
WORKDIR /app
COPY services/target/*.jar app.jar

CMD java -jar app.jar
