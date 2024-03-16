#!/bin/bash

REPOS_NAME="springlab"
ROLE_NAME="validator"
VERSION="0.1.0"
REPOS_ENDPOINT="475976321288.dkr.ecr.us-west-2.amazonaws.com"

## -- Runtime Variables
BUILD_ID=$(date +%Y%m%d-%H%M)
ARTIFACT_ENDPOINT="${REPOS_ENDPOINT}/${REPOS_NAME}"
DOCKER_IMAGE_NAME="${ARTIFACT_ENDPOINT}:${ROLE_NAME}_${VERSION}-${BUILD_ID}"


mvn spring-boot:build-image \
    -Dspring-boot.build-image.imageName=${DOCKER_IMAGE_NAME}

docker push ${DOCKER_IMAGE_NAME}

# 475976321288.dkr.ecr.us-west-2.amazonaws.com/member-service:web-api_0.3.0-20230909-1105
