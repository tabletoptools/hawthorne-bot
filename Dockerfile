FROM openjdk:8-jre
ARG BOT_TOKEN=NTEwMzY0OTA0MTA0OTE5MDQx.DtNZdA.85TDgdqc2PT6PMtLJeILfHMuv2A 
ARG TYPEFORM_TOKEN=5gajWqJ1yatrkbwveLrhpYrYm5NraRxwbzTxPMjZ5NPD 
ENV BOT_TOKEN=$BOT_TOKEN
ENV TYPEFORM_TOKEN=$TYPEFORM_TOKEN
ENV GOOGLE_APPLICATION_CREDENTIALS="credentials.json"
ENV env="production"
COPY target/hawthorne-bot-v195-jar-with-dependencies.jar hawthorne-bot.jar
COPY credentials.json credentials.json
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/urandom", "-jar", "/hawthorne-bot.jar"]