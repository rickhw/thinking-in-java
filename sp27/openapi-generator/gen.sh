#!/bin/bash

openapi-generator generate \
     -i spec/openapi-petstore.yaml \
     -g spring \
     -c codegen/spring-config.yaml \
     -o pet-out