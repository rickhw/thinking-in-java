#!/bin/bash

docker-compose down
docker-compose rm

rm -rf build

gradle clean build

# docker build .

docker-compose build
docker-compose up -d

./simulate.sh