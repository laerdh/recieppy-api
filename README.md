[![pipeline status](https://gitlab.com/laerdh/recieppy-api/badges/master/pipeline.svg)](https://gitlab.com/laerdh/recieppy-api/commits/master)
# recieppy-api

GraphQL API (OAuth2 Resource Server) for the Recieppy mobile apps.

##### Tech stack:
* Kotlin
* Spring boot
* PostgreSQL
* Flyway
* GraphQL java
* Firebase Admin

### Get started

##### Set up local PostgreSQL database
1. Configure a local PostgreSQL instance running on default port `5432`
2. Set the following environment variables in your IDE to connect to local database instance:
* `SERVER_PORT=<PORT> (default is 8000)`
* `DATABASE_URL=<URL>`
* `DATABASE_USER=<username>`
* `DATABASE_PASSWORD=<password>`
* `DATABASE_SCHEMA=<schema name>`
* `OAUTH2_JWT_ENDPOINT=<URL>`

This can easily be set up using Docker and Docker Compose.

##### Flyway migration
Flyway uses environment variables for the local database. No configuration needed.

##### Enable developer mode
Add `SPRING.PROFILES.ACTIVE=development` to run in dev mode.

