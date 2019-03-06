# recieppy-api

GraphQL API for the Recieppy mobile apps.

##### Tech stack:
* Kotlin
* Spring boot
* PostgreSQL
* Flyway
* GraphQL java
* JSON Web Token
* Firebase Admin

### Get started

##### Set up local PostgreSQL database
1. Configure a local PostgreSQL instance running on default port `5432`
2. Set the follwing environment variables in your IDE to connect to local database instance:
* `DATABASE_URL=<url>`
* `DATABASE_USER=<username>`
* `DATABASE_PASSWORD=<password>`
* `DATABASE_SCHEMA=<schema name>`
* `JWT_SECRET=<Generated secret>`

This can easily be set up using Docker and Docker Compose.

##### Flyway migration
Flyway uses environment variables for the local database. No configuration needed.

##### Enable developer mode
Add `SPRING.PROFILES.ACTIVE=development` to run in dev mode.

