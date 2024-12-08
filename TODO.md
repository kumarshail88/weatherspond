Update weather api with more http status codes for better error handling
The service design can be improved by decoupling the scheduler as a separate cron.
More integration tests needs to be added for scheduler, cache behavior and error handling.
Database (postgres) support can be added for storing the transformed weather data and optimized range queries.
No support for redis cluster. Standalone redis configuration is used.
Common rest client service can be added for making rest calls to external services. This 
will avoid code duplication and make the code more modular.
Refactor the code for better readability and maintainability.