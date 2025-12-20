# syntax=docker/dockerfile:1.7

############################
# 1) BUILD STAGE
############################
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Copy maven wrapper + pom trước để cache dependency tốt hơn
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Cấp quyền chạy mvnw
RUN chmod +x mvnw

# Tải dependencies trước (tận dụng cache; BuildKit sẽ cache ~/.m2)
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -q -DskipTests dependency:go-offline

# Copy source code rồi build
COPY src/ src/

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -q -DskipTests package

############################
# 2) RUNTIME STAGE
############################
FROM eclipse-temurin:25-jre
WORKDIR /app

# Set timezone (đúng timezone bạn đang dùng)
ENV TZ=Asia/Bangkok

# Tạo user không phải root để chạy trên K8s an toàn hơn
RUN addgroup --system --gid 10001 app \
 && adduser  --system --uid 10001 --ingroup app --home /app --disabled-password app

# Copy jar từ build stage
COPY --from=build /workspace/target/*.jar /app/app.jar

# Fix ownership
RUN chown -R app:app /app

USER 10001:10001

# Order-service của bạn chạy port 8084 (theo application.yaml)
EXPOSE 8084

# K8s sẽ inject env (DB, Kafka, JWT_SIGNER_KEY...) -> để JAVA_OPTS mở rộng khi cần
ENV JAVA_OPTS=""

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
