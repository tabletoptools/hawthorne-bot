#!/bin/bash
echo ${BUILD_VERSION};
gcloud docker -- push us.gcr.io/hawthorne-197812/hawthorne-bot:${BUILD_VERSION};
# gcloud beta compute instances update-container hawthorne-001 --zone=us-east1-b --container-image=us.gcr.io/hawthorne-197812/hawthorne-bot:${BUILD_VERSION};
# tar -czf credentials.tar.gz src/main/resources/client_secret.json deploy_secret.json credentials.json
# travis encrypt-file credentials.tar.gz --add -r tabletoptools/hawthorne-bot