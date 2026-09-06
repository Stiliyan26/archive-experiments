#If needed, compile new frontend version with:
#deploy_frontend_version.ps1
#Deploy the new JAR with the commands:
#mvn release:prepare
#you need to have the following in your Maven settings.xml:
<server>
    <id>gaia-reposilite</id>
    <username>admin</username>
    <password>cmLhLIZQK28TRA25BxFuGCWDVSTHVHMNrozHRcsago4G8Anhg5I2nS8zCDH0iNkd</password>
</server>
#mvn release:perform

Set-Location ./backend/
Copy-Item ./deploy/keystore_local.p12 ./deploy/docker/backend/config/keystore.p12
Copy-Item ./deploy/application.properties_test_evn ./deploy/docker/backend/config/application.properties
Copy-Item ./deploy/docker-compose_test_evn.yml ./deploy/docker/docker-compose.yml
Copy-Item ./deploy/Dockerfile_evn ./deploy/docker/Dockerfile
Copy-Item ./deploy/certificate_local.crt ./deploy/docker/backend/santa_crm.crt
Copy-Item ./deploy/get_latest_jar_evn.sh ./deploy/docker/backend/get_latest_jar.sh

#for prod - backup DB before version change
#docker exec nepal_nepal_postgres_1 pg_dump -h localhost -U postgres -d santa_crm > ./nepal_backup`date +\%Y-\%m-\%d`.sql

#log to the RDP server
#manually copy there in D:\Latona :
#./deploy/docker/* to .
#../frontend/dist/* to ./backend/public/
#use PuTTY to SSH on the test server
#use WinScp to copy the files to the test server /usr/latona
#on the test server in the app directory start:
#'cd ./backend/; ./get_latest_jar.sh'
#'docker compose -p selfie up -d --remove-orphans --build --force-recreate'
#docker compose -p selfie down

#sudo dnf install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
#sudo yum update
#sudo docker run hello-world
#sudo systemctl enable --now docker
#sudo dnf config-manager --add-repo https://download.docker.com/linux/rhel/docker-ce.repo
#sudo dnf -y install dnf-plugins-core
#sed -i 's/\r$//' get_latest_jar.sh
#chmod a+x get_latest_jar.sh
