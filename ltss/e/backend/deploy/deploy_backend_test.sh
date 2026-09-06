#!/bin/sh



cd ..
mvn clean install -Dmaven.test.skip=true
scp -i ~/.ssh/id_scripts_rsa -r ./deploy/docker/* angelvel@192.168.0.2:/home/angelvel/santa_crm_test/
scp -i ~/.ssh/id_scripts_rsa -r ./deploy/docker/docker-compose_test.yml angelvel@192.168.0.2:/home/angelvel/santa_crm_test/docker-compose.yml
scp -i ~/.ssh/id_scripts_rsa ./target/santa-1.0.0-SNAPSHOT.jar angelvel@192.168.0.2:/home/angelvel/santa_crm_test/backend/

ssh -i ~/.ssh/id_scripts_rsa angelvel@192.168.0.2 'docker build -t test_santa_crm_backend /home/angelvel/santa_crm_test/backend/'
ssh -i ~/.ssh/id_scripts_rsa angelvel@192.168.0.2 'cd /home/angelvel/santa_crm_test; docker-compose -p test up -d --remove-orphans'
