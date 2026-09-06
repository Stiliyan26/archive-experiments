#!/bin/sh
#the destination directory must be already created and executable for all chmod a+w
# mvn liquibase:diff should be started before this script to generate the differences between the DB and the source entity definitions
cd ../../frontend/
npm version patch
npm run test_bgmashini-build
cd ../backend/
cp ./deploy/keystore.p12 ./src/main/resources/
cp ./deploy/application.properties_test_bgmashini ./src/main/resources/application.properties
cp ./deploy/docker-compose_test_bgmashini.yml ./deploy/docker/docker-compose.yml
mvn clean install -Dmaven.test.skip=true
#ssh angelvel@37.157.143.162 -L 8080:192.168.0.1:80
scp -i ~/.ssh/id_scripts_rsa -r ./deploy/docker/* angelvel@37.157.143.162:/home/angelvel/bgmashini/
scp -i ~/.ssh/id_scripts_rsa ./target/santa-1.0.0-SNAPSHOT.jar angelvel@37.157.143.162:/home/angelvel/bgmashini/backend/
ssh -i ~/.ssh/id_scripts_rsa angelvel@37.157.143.162 'cd /home/angelvel/bgmashini; docker-compose -p bgmashini up -d --remove-orphans --build --force-recreate'
