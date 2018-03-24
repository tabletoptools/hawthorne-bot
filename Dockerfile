FROM openjdk:8-jre
ARG BOT_TOKEN
ARG TRAVIS_TAG
ENV BOT_TOKEN=$BOT_TOKEN
ADD target/hawthorne-bot-$TRAVIS_TAG.jar hawthorne-bot.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/hawthorne-bot.jar"]