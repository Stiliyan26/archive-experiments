#If needed, compile new frontend version with:
#deploy_frontend_version.ps1
#Deploy the new JAR with the commands:
#mvn release:prepare
#mvn release:perform

Set-Location ./backend/
Copy-Item ./deploy/keystore.p12 ./deploy/docker/backend/config/
Copy-Item ./deploy/application.properties_test_nepal ./deploy/docker/backend/config/application.properties
Copy-Item ./deploy/docker-compose_test_nepal.yml ./deploy/docker/docker-compose.yml
Copy-Item ./deploy/certificate.crt ./deploy/docker/backend/santa_crm.crt
Copy-Item ./deploy/get_latest_jar_nepal.sh ./deploy/docker/backend/get_latest_jar.sh


#ssh angelvel@37.157.143.162 -L 8080:192.168.0.1:80

#commands for enabling key for login to the server
#ssh-keygen -t rsa -b 2048
#type $env:USERPROFILE\.ssh\id_rsa.pub | ssh angelvel@37.157.143.162 "cat >> .ssh/authorized_keys"
#ssh -i $env:USERPROFILE\.ssh\id_rsa angelvel@37.157.143.162
ssh -i $env:USERPROFILE\.ssh\id_rsa angelvel@37.157.143.162 'docker exec nepal_nepal_postgres_1 pg_dump -h localhost -U postgres -d santa_crm > ./nepal_backup`date +\%Y-\%m-\%d`.sql'
#scp -i $env:USERPROFILE\.ssh\id_rsa angelvel@37.157.143.162:./nepal_backup* .
#scp -i $env:USERPROFILE\.ssh\id_rsa ./nepal_backup* angelvel@37.157.143.162:/home/angelvel/nepal/
#cat nepal_backup2024-12-19.sql | docker exec -i nepal_nepal_postgres_1  bash -c 'psql -U santa_crm -d santa_crm'
scp -i $env:USERPROFILE\.ssh\id_rsa -r ./deploy/docker/* angelvel@37.157.143.162:/home/angelvel/nepal/
scp -i $env:USERPROFILE\.ssh\id_rsa -r ../frontend/dist/* angelvel@37.157.143.162:/home/angelvel/nepal/backend/public/
ssh -i $env:USERPROFILE\.ssh\id_rsa angelvel@37.157.143.162 'cd /home/angelvel/nepal/backend/; ./get_latest_jar.sh'
ssh -i $env:USERPROFILE\.ssh\id_rsa angelvel@37.157.143.162 'cd /home/angelvel/nepal; docker-compose -p nepal up -d --remove-orphans --build --force-recreate'
