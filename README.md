# recieppy-api

GraphQL API for the Recieppy mobile apps.

### Get started

##### Set up local database
1. Configure a local PostgreSQL instance running on default port `5432`
2. Set the follwing environment variables in your IDE to connect to local database instance:
* `DATABASE_URL=<url>`
* `DATABASE_USER=<username>`
* `DATABASE_PASSWORD=<password>`

This can easily be set up using Docker and Docker Compose.

##### Flyway migration
Add `flyway.properties` to project root and define properties:
* `url = <databaseurl>`
* `user = <username>`
* `password = <password>`
* `schemas = <schemas>`

##### Enable developer mode
Add `SPRING.PROFILES.ACTIVE=development` to run in dev mode.
