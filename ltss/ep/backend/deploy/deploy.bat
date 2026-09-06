"C:\Users\angel velikov\Desktop\PuTTYPortable\App\putty\pscp" -r -scp -pw CRMsanta.12 * santa_crm@172.30.11.4:/home/santa_crm/

"C:\Users\angel velikov\Desktop\PuTTYPortable\App\putty\plink" -ssh -pw CRMsanta.12 santa_crm@172.30.11.4 "chmod +x *.sh"
"C:\Users\angel velikov\Desktop\PuTTYPortable\App\putty\plink" -ssh -pw CRMsanta.12 santa_crm@172.30.11.4 "docker build -t docker_santa_postgres /home/santa_crm/docker/postgres/"
"C:\Users\angel velikov\Desktop\PuTTYPortable\App\putty\plink" -ssh -pw CRMsanta.12 santa_crm@172.30.11.4 "docker build -t docker_santa_crm_backend /home/santa_crm/docker/backend/"
"C:\Users\angel velikov\Desktop\PuTTYPortable\App\putty\plink" -ssh -pw CRMsanta.12 santa_crm@172.30.11.4 "cd /home/santa_crm/docker; docker-compose -p prod up -d --remove-orphans"
pause