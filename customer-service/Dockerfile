# =========================
# 1) BUILD STAGE
# =========================
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Cài curl (mvnw có thể cần để tải maven-wrapper/maven distribution)
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Copy các file cần thiết để tận dụng cache dependencies
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Tải dependencies trước (tối ưu cache cho CI)
RUN chmod +x mvnw \
    && ./mvnw -q -DskipTests dependency:go-offline

# Copy source sau cùng để tránh mất cache khi code đổi
COPY src/ src/

# Build jar
RUN ./mvnw -q -DskipTests clean package


# =========================
# 2) RUNTIME STAGE
# =========================
FROM eclipse-temurin:25-jre

WORKDIR /app

# Best-practice: chạy non-root
RUN useradd -r -u 10001 appuser
USER 10001

# App dùng port 8083 theo application.yaml
EXPOSE 8083

# Tùy chọn: cấu hình JVM hợp k8s (tôn trọng memory limits), timezone VN/Thailand
ENV TZ=Asia/Bangkok
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=25 -Duser.timezone=Asia/Bangkok"

# Copy jar từ build stage
COPY --from=build /app/target/*.jar /app/app.jar

# Run
ENTRYPOINT ["java","-jar","/app/app.jar"]
