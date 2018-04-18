FROM openjdk:8-jre
ARG BOT_TOKEN
ARG BUILD_VERSION
ARG TYPEFORM_TOKEN
ENV BOT_TOKEN=$BOT_TOKEN
ENV TYPEFORM_TOKEN=$TYPEFORM_TOKEN
ADD target/hawthorne-bot-${BUILD_VERSION}-jar-with-dependencies.jar hawthorne-bot.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/hawthorne-bot.jar"]