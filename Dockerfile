FROM openjdk:8-jre
ARG BOT_TOKEN
ARG BUILD_VERSION
ENV BOT_TOKEN=$BOT_TOKEN
ADD target/hawthorne-bot-${BUILD_VERSION}-jar-with-dependencies.jar hawthorne-bot.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/hawthorne-bot.jar"]