# Weatherspond service
Spond's weather service that serves the weather data for the Spond listed events.   

## Tech stack
1. **Springboot**

## Prerequisite

- Java 17 or higher  
- Intellij(recommended) or similar Java based IDE
- Docker (and Docker desktop for macOS)

## Development environment setup
1. Configure env variables or .env. 
2. Test Environment configuration: 
    - `.env` and `.env.properties` for tests.

# Running the application locally
1. **Build the project:**  
```shell
./gradlew clean build
```
2. **Start the service:**
```shell
./gradlew bootRun --args='--spring.profiles.active=local'
```

# Running with docker-compose

## Testing

## Logging

## Monitoring

## Application properties

## Environment variables

## Caching

## Dependency management





