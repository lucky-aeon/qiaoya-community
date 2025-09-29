# ========== 构建阶段 ==========
FROM maven:3.9.8-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests dependency:go-offline --no-transfer-progress
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -T 1C -DskipTests package --no-transfer-progress

# ========== 运行阶段 ==========
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/*.jar /app/app.jar
ENV JAVA_TOOL_OPTIONS "-Duser.timezone=Asia/Shanghai"
EXPOSE 8520
ENTRYPOINT ["sh", "-c", "java $JAVA_TOOL_OPTIONS -jar /app/app.jar"]
