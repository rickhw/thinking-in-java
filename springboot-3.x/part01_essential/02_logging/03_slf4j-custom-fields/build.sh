#!/bin/bash

source ./scripts/build-config.sh

rm -rf target

#mvn clean install
#mvn clean install -DBUILD_TYPE=dev -DHASH_CODE=${HASH_CODE}
mvn clean package \
    -DBUILD_TYPE=${BUILD_TYPE} \
    -DHASH_CODE=${HASH_CODE} \
    -DBUILD_ID=${BUILD_ID}

