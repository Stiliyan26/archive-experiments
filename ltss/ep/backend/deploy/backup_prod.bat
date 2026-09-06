"C:\Users\angel velikov\Desktop\PuTTYPortable\App\putty\putty" -ssh -2 -pw CRMsanta.12 santa_crm@172.30.11.4 -m backup_prod.sh
"C:\Users\angel velikov\Desktop\PuTTYPortable\App\putty\pscp" -r -scp -pw CRMsanta.12 santa_crm@172.30.11.4:/home/santa_crm/backup/dump_santa_crm.sql .
pause