FROM openjdk:8-jre
ARG BOT_TOKEN
ARG BUILD_VERSION
ARG TYPEFORM_TOKEN
ENV BOT_TOKEN=$BOT_TOKEN
ENV TYPEFORM_TOKEN=$TYPEFORM_TOKEN
ENV env="production"
ENV SQREEN_TOKEN=$SQREEN_TOKEN
ADD target/hawthorne-bot-${BUILD_VERSION}-jar-with-dependencies.jar hawthorne-bot.jar
ADD sqreen.jar sqreen.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/hawthorne-bot.jar"]