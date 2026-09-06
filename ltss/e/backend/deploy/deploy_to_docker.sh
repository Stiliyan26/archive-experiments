#!/bin/sh
#the destination directory must be already created and executable for all chmod a+w
# mvn liquibase:diff should be started before this script to generate the differences between the DB and the source entity definitions
cd ../../frontend/
npm version minor
npm run dist-build
cd ../backend/
cp ./deploy/keystore_prod.p12 ./src/main/resources/keystore.p12
cp ./deploy/application.properties_prod ./src/main/resources/application.properties
mvn clean install -Dmaven.test.skip=true
#ssh angelvel@178.18.245.97 -L 8080:192.168.0.1:80
scp -i ~/.ssh/id_scripts_rsa -r ./deploy/docker/* angelvel@178.18.245.97:/home/angelvel/translogic/

scp -i ~/.ssh/id_scripts_rsa ./target/santa-1.0.0-SNAPSHOT.jar angelvel@178.18.245.97:/home/angelvel/translogic/backend/
#ssh -i ~/.ssh/id_scripts_rsa angelvel@178.18.245.97 'docker build -t prod_translogic_postgres /home/angelvel/translogic/postgres/'
#ssh -i ~/.ssh/id_scripts_rsa angelvel@178.18.245.97 'docker build -t prod_translogic_backend /home/angelvel/translogic/backend/'
ssh -i ~/.ssh/id_scripts_rsa angelvel@178.18.245.97 'cd /home/angelvel/translogic; docker-compose -p prod up -d --remove-orphans --build --force-recreate'
