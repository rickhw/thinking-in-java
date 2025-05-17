#!/bin/bash

## docker register
REPOS_ENDPOINT="475976321288.dkr.ecr.us-west-2.amazonaws.com"
REPOS_NAME="rws-dev"

## product metadata
PRODUCT_NAME="rws"
ROLE_NAME="booter"
VERSION="0.2.0"
BUILD_TYPE="dev"

## -- Runtime Variables
HASH_CODE=$(git rev-parse HEAD)
BUILD_ID=$(date +%Y%m%d-%H%M)

if [ "${BUILD_TYPE}" == "rel" ]; then
    REPOS_NAME="rws"
fi

## Artifact metadata
ARTIFACT_ENDPOINT="${REPOS_ENDPOINT}/${REPOS_NAME}"
DOCKER_IMAGE_NAME="${ARTIFACT_ENDPOINT}:${PRODUCT_NAME}.${ROLE_NAME}_v${VERSION}-${BUILD_TYPE}-b${BUILD_ID}"

