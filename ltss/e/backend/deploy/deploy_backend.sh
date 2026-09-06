#!/bin/sh



cd ..
mvn clean install -Dmaven.test.skip=true
scp -i ~/.ssh/id_scripts_rsa -r ./deploy/docker/* angelvel@192.168.0.2:/home/angelvel/santa_crm/
scp -i ~/.ssh/id_scripts_rsa ./target/santa-1.0.0-SNAPSHOT.jar angelvel@192.168.0.2:/home/angelvel/santa_crm/backend/

ssh -i ~/.ssh/id_scripts_rsa angelvel@192.168.0.2 'docker build -t prod_santa_crm_backend /home/angelvel/santa_crm/backend/'
ssh -i ~/.ssh/id_scripts_rsa angelvel@192.168.0.2 'cd /home/angelvel/santa_crm; docker-compose -p prod up -d --remove-orphans'
