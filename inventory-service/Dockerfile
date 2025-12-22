# syntax=docker/dockerfile:1.7

############################
# 1) BUILD STAGE
############################
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# ===== 1. Build & install saga-messages =====
COPY saga-messages /workspace/saga-messages
WORKDIR /workspace/saga-messages

RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw && ./mvnw -Dmaven.test.skip=true clean install

# ===== 2. Build inventory-service =====
WORKDIR /workspace/inventory-service

COPY inventory-service/mvnw inventory-service/pom.xml ./
COPY inventory-service/.mvn .mvn

RUN chmod +x mvnw

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -Dmaven.test.skip=true dependency:go-offline

COPY inventory-service/. .

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -Dmaven.test.skip=true clean package

############################
# 2) RUNTIME STAGE
############################
FROM eclipse-temurin:25-jre
WORKDIR /app

ENV TZ=Asia/Bangkok
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=25"

RUN addgroup --system --gid 10001 app \
 && adduser  --system --uid 10001 --ingroup app --home /app --disabled-password app

COPY --from=build /workspace/inventory-service/target/*.jar /app/app.jar

RUN chown -R app:app /app
USER 10001:10001

EXPOSE 8085
ENTRYPOINT ["java","-jar","/app/app.jar"]