#If needed, compile new frontend version with:
#deploy_frontend_version.ps1
#Deploy the new JAR with the commands:
#mvn release:prepare
#mvn release:perform

Set-Location ./backend/
Copy-Item ./deploy/keystore_nepal.p12 ./deploy/docker/backend/config/keystore.p12
Copy-Item ./deploy/application.properties_nepal ./deploy/docker/backend/config/application.properties
Copy-Item ./deploy/docker-compose_nepal.yml ./deploy/docker/docker-compose.yml
Copy-Item ./deploy/certificate_nepal.crt ./deploy/docker/backend/santa_crm.crt
Copy-Item ./deploy/get_latest_jar_nepal.sh ./deploy/docker/backend/get_latest_jar.sh


#ssh angelvel@51.68.172.237

#commands for enabling key for login to the server
#ssh-keygen -t rsa -b 2048
#type $env:USERPROFILE\.ssh\id_rsa.pub | ssh centos@51.68.172.237 "cat >> .ssh/authorized_keys"
#ssh -i $env:USERPROFILE\.ssh\id_rsa centos@51.68.172.237
ssh -i $env:USERPROFILE\.ssh\id_rsa centos@51.68.172.237 'docker exec nepal_nepal_postgres_1 pg_dump -h localhost -U postgres -d santa_crm > ./nepal_backup`date +\%Y-\%m-\%d`.sql'
#scp -i $env:USERPROFILE\.ssh\id_rsa centos@51.68.172.237:./nepal_backup* .
#on the server the directories /home/centos/nepal/backend/public should be existing and get_latest_jar.sh should be made executable
scp -i $env:USERPROFILE\.ssh\id_rsa -r ./deploy/docker/* centos@51.68.172.237:/home/centos/nepal/
scp -i $env:USERPROFILE\.ssh\id_rsa -r ../frontend/dist/* centos@51.68.172.237:/home/centos/nepal/backend/public/
ssh -i $env:USERPROFILE\.ssh\id_rsa centos@51.68.172.237 'cd /home/centos/nepal/backend/; ./get_latest_jar.sh'
ssh -i $env:USERPROFILE\.ssh\id_rsa centos@51.68.172.237 'cd /home/centos/nepal; docker-compose -p nepal up -d --remove-orphans --build --force-recreate'
