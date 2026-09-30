# =====================================================================
# innovaFeed — imagen Docker para producción (Render)
# Se construye en 2 etapas: una con Maven para compilar y otra, más liviana,
# solo con Java para ejecutar. Así la imagen final no incluye Maven ni el código fuente.
# =====================================================================

# ---------- Etapa 1: compilar ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Primero solo el pom.xml: Docker guarda en caché las dependencias descargadas
# y no las vuelve a bajar si únicamente cambia el código.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
# Las pruebas se corren localmente con "mvn test"; aquí se omiten para construir más rápido
RUN mvn -q -B package -DskipTests

# ---------- Etapa 2: ejecutar ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Buena práctica de seguridad: la app no corre como root
RUN useradd --system --uid 1001 innovafeed
COPY --from=build /app/target/innovafeed-*.jar app.jar
USER innovafeed

ENV SPRING_PROFILES_ACTIVE=prod
# Hora de Colombia: los servidores de Render usan UTC (5 horas adelante)
ENV TZ=America/Bogota
# Ajustes de memoria para instancias pequeñas (el plan gratis de Render tiene 512 MB)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=60 -XX:+UseSerialGC -Xss512k -Duser.timezone=America/Bogota"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
