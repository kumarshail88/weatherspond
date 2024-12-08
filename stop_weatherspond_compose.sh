#!/bin/bash

./gradlew clean build
docker compose -f docker-compose/compose.yaml down --remove-orphans