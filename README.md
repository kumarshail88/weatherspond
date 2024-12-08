# Weatherspond service
Spond's weather service that serves the weather data for the Spond listed events.   

## Tech stack
1. **Springboot**
2. **Java 17**
3. **Gradle**
4. **Docker**
5. **Redis**

## Prerequisite

- Java 17 or higher  
- Intellij(recommended) or similar Java based IDE
- Docker (and Docker desktop for macOS)

## Development environment setup
1. Configure env variables or .env. 
2. Test Environment configuration: 
    - `.env` and `.env.properties` for tests.

# Running the application locally
The application can run locally in 2 ways: 1. Using the IDE and 2. Using the docker-compose.  
### Start the application using the IDE or terminal.  
1. **Build the project:**  
```shell
./gradlew clean build
```
2. **Start the docker compose for redis container:**  
```
docker compose -f ./docker-compose/compose.yaml up --build --force-recreate -d
```
3. **Start the service:**
```shell
./gradlew bootRun --args='--spring.profiles.active=local'
```
!!NOTE: The service has platform dependency over jq. Please ensure that jq is installed on your system. If not then use the docker-compose method to run the service.
jq can be installed using the following command on macOS:  
```shell
brew install jq
```
On linux:  
```shell
sudo apt-get install jq -y
```

The local server is reachable at `http://localhost:8080/api/v1`  
Weather api is reachable at `http://localhost:8080/api/v1/weather/forecast  
Example curl request:  
```shell
curl --location 'localhost:8080/api/v1/weather/forecast?lat=60.5&lon=11.59' \
--header 'Content-Type: application/json' \
--data '{
    "events": [
        {
            "id": 101,
            "start": "2024-12-09T15:00:00Z",
            "end": "2024-12-08T18:00:00Z"
        },
        {
            "id": 102,
            "start": "2024-12-11T11:00:00Z",
            "end": "2024-12-11T18:00:00Z"
        }
    ]
}'
```  

# Running with docker-compose
1. **Simply run the script:**  
```shell
./start_weatherspond_compose.sh
```
2. **Stop the docker compose and service:**  
```shell
./stop_weatherspond_compose.sh
```
The docker compose weatherspond-service is reachable at `http://localhost:8081/api/v1`

## Testing
1. **Run the tests:**  
```shell
./gradlew test
```
Tests require the test containers setup and docker. Ensure that the docker compose is fully stopped
before running the tests. This is for the test containers to work properly.

## Logging
Logging is done using log4j2. For now only console logging is enabled. Logs are only
configured for local and test profiles.

## Monitoring
Not implemented yet.

## Application properties
Refer to the `application.yaml` and `application-local.yaml` file for the properties.

## Environment variables
Environment variables can be used to override some of the properties. .env file can be used to set the environment variables.
Server

## Caching
Redis is used for caching the weather data. The cache is set to expire after 2 hours.
The service also uses a simple in memory local cache for the scheduler to periodically update the weather data cache.
The scheduler runs every 2 hours. The scheduler is not well supported or efficiently designed to run in 
a distributed environment.

## Dependency management
Spingboot and gradle are used for dependency management. The dependencies are defined in the `settings.gradle` `build.gradle` file.
For spring supported dependencies, spring dependency management plugin is used.

## Improvements and limitations
Refer to the TODO.md file for the improvements and limitations.

## Code quality and formatting
The code quality is maintained using locally installed sonar plugin and formatting 
is automatically done by the spotless formatter plugin. 
To format the code simply run 
```shell
./gradlew spotelessApply'
```
