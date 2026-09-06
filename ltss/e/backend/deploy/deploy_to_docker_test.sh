#!/bin/sh
#the destination directory must be already created and executable for all chmod a+w
# mvn liquibase:diff should be started before this script to generate the differences between the DB and the source entity definitions
cd ../../frontend/
NEW_VER=$(npm version patch)
npm run staging-build
cd ../backend/
cp ./deploy/keystore.p12 ./src/main/resources/
cp ./deploy/application.properties_test ./src/main/resources/application.properties
mvn clean install -Dmaven.test.skip=true
#ssh angelvel@37.157.143.162 -L 8080:192.168.0.1:80
scp -i ~/.ssh/id_scripts_rsa -r ./deploy/docker/* angelvel@37.157.143.162:/home/angelvel/translogic/
scp -i ~/.ssh/id_scripts_rsa -r ./deploy/docker/docker-compose_test.yml angelvel@37.157.143.162:/home/angelvel/translogic/docker-compose.yml
scp -i ~/.ssh/id_scripts_rsa ./target/santa-1.0.0-SNAPSHOT.jar angelvel@37.157.143.162:/home/angelvel/translogic/backend/
#ssh -i ~/.ssh/id_scripts_rsa angelvel@37.157.143.162 'docker build -t test_translogic_postgres /home/angelvel/translogic/postgres/'
#ssh -i ~/.ssh/id_scripts_rsa angelvel@37.157.143.162 'docker build -t test_translogic_backend /home/angelvel/translogic/backend/'
ssh -i ~/.ssh/id_scripts_rsa angelvel@37.157.143.162 'cd /home/angelvel/translogic; docker-compose -p test up -d --remove-orphans --build --force-recreate'
