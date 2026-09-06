#If needed, compile new frontend version with:
#deploy_frontend_version.ps1
#Deploy the new JAR with the commands:
#mvn release:prepare
#mvn release:perform

Set-Location ./backend/
Copy-Item ./deploy/keystore_local.p12 ./deploy/docker/backend/config/keystore.p12
Copy-Item ./deploy/application.properties_test_anvoice ./deploy/docker/backend/config/application.properties
Copy-Item ./deploy/docker-compose_test_anvoice.yml ./deploy/docker/docker-compose.yml
Copy-Item ./deploy/Dockerfile_anvoice ./deploy/docker/Dockerfile
Copy-Item ./deploy/certificate_local.crt ./deploy/docker/backend/santa_crm.crt
Copy-Item ./deploy/get_latest_jar_anvoice.sh ./deploy/docker/backend/get_latest_jar.sh

#for prod - backup DB before version change
#docker exec anvoice_anvoice_postgres_1 pg_dump -h localhost -U postgres -d santa_crm > ./anvoice_backup`date +\%Y-\%m-\%d`.sql

#commands for enabling key for login to the server
#ssh-keygen -t rsa -b 2048
#type $env:USERPROFILE\.ssh\id_rsa.pub | ssh latona@10.236.19.27 "mkdir -p .ssh && cat >> .ssh/authorized_keys"
#ssh -i $env:USERPROFILE\.ssh\id_rsa latona@10.236.19.27
#chmod a+x get_latest_jar.sh
#sed -i 's/\r$//' get_latest_jar.sh
#sudo snap install docker
#?#sudo apt  install docker-compose
#?#sudo systemctl enable docker.service
#?#sudo systemctl start docker

#sudo docker-compose -p anvoice down
scp -i $env:USERPROFILE\.ssh\id_rsa -r ./deploy/docker/* latona@10.236.19.27:/home/latona/anvoice/
scp -i $env:USERPROFILE\.ssh\id_rsa -r ../frontend/dist/* latona@10.236.19.27:/home/latona/anvoice/backend/public/
ssh -i $env:USERPROFILE\.ssh\id_rsa latona@10.236.19.27 'cd /home/latona/anvoice/backend/; ./get_latest_jar.sh'
ssh -i $env:USERPROFILE\.ssh\id_rsa latona@10.236.19.27 'cd /home/latona/anvoice; sudo docker-compose -p anvoice up -d --remove-orphans --build --force-recreate'
