#!/bin/sh
#the destination directory must be already created and executable for all chmod a+w
# mvn liquibase:diff should be started before this script to generate the differences between the DB and the source entity definitions
cd ../../frontend/
npm version patch
npm run dist_bgmashini-build
cd ../backend/
cp ./deploy/keystore_om.p12 ./src/main/resources/keystore.p12
cp ./deploy/application.properties_bgmashini ./src/main/resources/application.properties
cp ./deploy/docker-compose_bgmashini.yml ./deploy/docker/docker-compose.yml
mvn clean install -Dmaven.test.skip=true
#ssh root@195.201.4.80 -L 8080:192.168.0.1:80
scp -i ~/.ssh/id_scripts_rsa -r ./deploy/docker/* root@195.201.4.80:/www/latona/
scp -i ~/.ssh/id_scripts_rsa ./target/santa-1.0.0-SNAPSHOT.jar root@195.201.4.80:/www/latona/backend/
ssh -i ~/.ssh/id_scripts_rsa root@195.201.4.80 'cd /www/latona; docker compose -p bgmashini up -d --remove-orphans --build --force-recreate'
