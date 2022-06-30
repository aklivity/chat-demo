FROM eclipse-temurin:17-alpine

COPY target/chat-service.jar /

RUN apk upgrade --no-cache && \
    apk add --no-cache libgcc libstdc++ ncurses-libs

ENTRYPOINT ["java", "-jar", "/chat-service.jar"]
