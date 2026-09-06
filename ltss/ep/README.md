SANTA software system
Latona Ltd.

Architecture
Backend: Java
Relational DB: Postgres
Web frontend: React
Integration module: Apache Camel

Deployment (standard)
Using Docker with two containers in composition - for DB and for application
The Java application hosts the frontend and the integration.
Done using one of the "deploy_*" scripts in the "deploy" directory.

Local (debug) usage
Install Java 8
Install Maven

Install PostgreSQL (min 9.6). Use the SQL scripts in backend/deploy/docker/postgres to create the required DB.
Set up your computer (in etc hosts) to resolve the santa_crm_db host to localhost. Add line "127.0.0.1    santa_crm_db" to "C:\Windows\System32\drivers\etc\hosts".
To create the DB (first time run) change in application.properties:
spring.jpa.hibernate.ddl-auto=create
hibernate.hbm2ddl.auto=create

Backend start: mvn spring-boot:run

Install Node.js from https://nodejs.org/en/download/package-manager/, select version v14.21.3

Install Babel package:
npm install babel-cli@6.26.0
You may also need to run:
rm package-lock.json
npm install
npm rebuild node-sass

Frontend start: npm run dev
Local URL: http://127.0.0.1:2337


Backend features
Create Entities and Repository interfaces only.
Lombok generates standard Entity methods.
Hibernate provides JPA access to the DB.
Envers provides Entity change log.
Java Spring Data REST - provides Entities with Repositories and RESTful endpoints.
QueryDSL provides filtering for endpoints and improved query building.
Drools provides business rules handling. All validation, default values and processes must be in Drools rules.
Session security using JSON web tokens.

Git:

http://37.157.143.162:11080/latona/santa/

Test Translogic:

https://37.157.143.162:41443/translogic/

Setup:

node v14.17.3

npm 6.14.13

Install NVM 0.34.0:
curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.34.0/install.sh | bash

Install Node:
nvm install 14.17.3

