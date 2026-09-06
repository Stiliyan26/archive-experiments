#echo "Starting backup `date +\%Y-\%m-\%d`" >> santa_crm_backup.log
#find /home/santa_crm_test/backup/*.sql -mtime +60 -exec rm -R '{}' ';'
ssh -i ~/.ssh/id_scripts_rsa angelvel@37.157.143.162 "docker exec bgmashini_bgmashini_postgres_1 bash -c 'pg_dumpall -U postgres' > /home/angelvel/dump_santa_bgmashini.sql"
scp -i ~/.ssh/id_scripts_rsa -r angelvel@37.157.143.162:/home/angelvel/dump_santa_bgmashini.sql ../../dump_santa_bgmashini_"`date +\%Y-\%m-\%d`".sql
#echo "Finished backup `date +\%Y-\%m-\%d`" >> santa_crm_backup.log