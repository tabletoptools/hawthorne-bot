#!/bin/bash
echo ${BUILD_VERSION};
gcloud docker -- push us.gcr.io/hawthorne-197812/hawthorne-bot:${BUILD_VERSION};
# gcloud beta compute instances update-container hawthorne-001 --zone=us-east1-b --container-image=us.gcr.io/hawthorne-197812/hawthorne-bot:${BUILD_VERSION};