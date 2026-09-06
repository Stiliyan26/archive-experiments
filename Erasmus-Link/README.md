Required Apps and software:
1.PostgreSQL 17 | https://www.postgresql.org/download/
2.pgAdmin4 | https://www.pgadmin.org/download/pgadmin-4-windows/
3.Any IDE of your choice

Setup:
1.Clone the Erasmus-Link repo
2.Open pgAdmin4 and make a new database (e.g. ErasmusLink)
3.Run the ErasmusLinkDBScript query to create user and grant permissions (ask for the file)
4.Open the Erasmus-Link repo locally
5.Run "./gradlew clean --refresh-dependencies"
6.Open config/application.properties in the root directory
7.Change the spring.datasource.url property accordinglt (port and the database name)
8.Open the ui folder and run "npm install --legacy-peer-deps" and "npm run dev"
9.Go back to the root folder and run ./gradlew bootRun