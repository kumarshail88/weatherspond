#!/bin/bash

./gradlew clean build
docker compose -f ./docker-compose/compose.yaml up --build --force-recreate -d

