FROM eclipse-temurin:17-jre
WORKDIR /app
# CI runs Maven before docker build; keep image creation to a fast jar copy.
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app/app.jar
ENV JAVA_TOOL_OPTIONS "-Duser.timezone=Asia/Shanghai"
EXPOSE 8520
ENTRYPOINT ["sh", "-c", "java $JAVA_TOOL_OPTIONS -jar /app/app.jar"]
