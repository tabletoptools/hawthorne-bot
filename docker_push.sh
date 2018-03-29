#!/bin bash
gcloud docker -- push us.gcr.io/hawthorne-197812/hawthorne-bot:${TRAVIS_TAG};
gcloud beta compute instances update-container hawthorne-001 --zone=us-east1-b --container-image=us.gcr.io/hawthorne-197812/hawthorne-bot:${TRAVIS_TAG};