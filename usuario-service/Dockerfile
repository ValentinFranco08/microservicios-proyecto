## Dockerfile para el servicio de Usuario - Define la imagen base de Maven para construir la aplicación, copia el código fuente, instala las dependencias y empaqueta la aplicación. Luego, utiliza una imagen de JRE para ejecutar la aplicación, exponiendo el puerto 8001 para el servicio de Usuario.
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src/ src/
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8001

ENTRYPOINT ["java", "-jar", "app.jar"]
