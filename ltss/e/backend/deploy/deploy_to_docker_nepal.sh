#!/bin/sh
#the destination directory must be already created and executable for all chmod a+w
# mvn liquibase:diff should be started before this script to generate the differences between the DB and the source entity definitions
cd ../../frontend/
npm version patch
npm run dist_nepal-build
cd ../backend/
cp ./deploy/keystore_nepal.p12 ./src/main/resources/keystore.p12
cp ./deploy/application.properties_nepal ./src/main/resources/application.properties
cp ./deploy/docker-compose_nepal.yml ./deploy/docker/docker-compose.yml
cp ./deploy/certificate_nepal.crt ./deploy/docker/backend/santa_crm.crt
mvn clean install -Dmaven.test.skip=true
#create SSH key to access the server:
#ssh-keygen -t rsa -b 2048 -f ~/.ssh/id_scripts_rsa
#copy SSH key to the server:
#ssh-copy-id -i ~/.ssh/id_scripts_rsa centos@51.68.172.237
scp -i ~/.ssh/id_scripts_rsa -r ./deploy/docker/* centos@51.68.172.237:/home/centos/nepal/
scp -i ~/.ssh/id_scripts_rsa ./target/santa-1.0.0-SNAPSHOT.jar centos@51.68.172.237:/home/centos/nepal/backend/
ssh -i ~/.ssh/id_scripts_rsa centos@51.68.172.237 'cd /home/centos/nepal; docker-compose -p nepal up -d --remove-orphans --build --force-recreate'
