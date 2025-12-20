# syntax=docker/dockerfile:1.6

############################
# 1) Build stage
############################
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Copy maven wrapper + pom trước để cache dependency tốt hơn
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# (BuildKit cache) Nếu CI của bạn chưa bật BuildKit thì dòng --mount sẽ bị bỏ qua theo cách bạn build.
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -q -DskipTests dependency:go-offline

# Copy source sau cùng (để đổi code không invalidate cache dependency)
COPY src/ src/

# Build jar
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -q -DskipTests package

# Extract layers (Spring Boot layertools)
# Spring Boot 4 vẫn hỗ trợ jarmode=tools (layertools)
RUN mkdir -p /layers && \
    java -Djarmode=tools -jar target/*.jar extract --layers --destination /layers


############################
# 2) Runtime stage
############################
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# Tạo user non-root (chuẩn deploy K8s)
RUN groupadd -g 10001 app && \
    useradd  -u 10001 -g app -s /sbin/nologin -m app

# Copy extracted layers để tối ưu cache/push image
COPY --from=build /layers/dependencies/ /app/
COPY --from=build /layers/snapshot-dependencies/ /app/
COPY --from=build /layers/spring-boot-loader/ /app/
COPY --from=build /layers/application/ /app/

# Port theo application.yaml
EXPOSE 8085

# JVM opts có thể override qua env trong K8s (Deployment env)
# Ví dụ: JAVA_OPTS: "-Xms256m -Xmx512m"
ENV JAVA_OPTS=""

USER 10001:10001

# Run theo cơ chế launcher của Spring Boot (khi đã extract layers)
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
