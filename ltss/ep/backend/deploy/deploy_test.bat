"C:\Users\angel velikov\Desktop\PuTTYPortable\App\putty\pscp" -r -scp -pw CRMsanta.12 * santa_crm@172.30.11.4:/home/santa_crm_test/
"C:\Users\angel velikov\Desktop\PuTTYPortable\App\putty\pscp" -r -scp -pw CRMsanta.12 .\docker\docker-compose_test.yml santa_crm@172.30.11.4:/home/santa_crm_test/docker/docker-compose.yml
"C:\Users\angel velikov\Desktop\PuTTYPortable\App\putty\plink" -ssh -pw CRMsanta.12 santa_crm@172.30.11.4 "chmod +x *.sh"
"C:\Users\angel velikov\Desktop\PuTTYPortable\App\putty\plink" -ssh -pw CRMsanta.12 santa_crm@172.30.11.4 "docker build -t test_santa_postgres /home/santa_crm_test/docker/postgres/"
"C:\Users\angel velikov\Desktop\PuTTYPortable\App\putty\plink" -ssh -pw CRMsanta.12 santa_crm@172.30.11.4 "docker build -t test_santa_crm_backend /home/santa_crm_test/docker/backend/"
"C:\Users\angel velikov\Desktop\PuTTYPortable\App\putty\plink" -ssh -pw CRMsanta.12 santa_crm@172.30.11.4 "cd /home/santa_crm_test/docker; docker-compose -p test up -d --remove-orphans"
pause