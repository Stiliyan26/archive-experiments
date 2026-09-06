echo "Starting backup `date +\%Y-\%m-\%d`" >> /home/backup/translogic/santa_backup.log
find /home/backup/translogic/*.sql -mtime +60 -exec rm -R '{}' ';'
ssh -i ~/.ssh/id_scripts_rsa backup@178.18.245.97 "docker exec prod_translogic_postgres_1 bash -c 'pg_dumpall -U postgres' > /home/backup/dump_santa_translogic.sql"
scp -i ~/.ssh/id_scripts_rsa backup@178.18.245.97:/home/backup/dump_santa_translogic.sql /home/backup/translogic/dump_santa_translogic"`date +\%Y-\%m-\%d`".sql
echo "Finished backup `date +\%Y-\%m-\%d`" >> /home/backup/translogic/santa_backup.log