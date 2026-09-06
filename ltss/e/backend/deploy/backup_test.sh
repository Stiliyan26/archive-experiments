echo "Starting backup `date +\%Y-\%m-\%d`" >> santa_crm_backup.log
find /home/santa_crm_test/backup/*.sql -mtime +60 -exec rm -R '{}' ';'
docker exec test_santa_postgres_1 bash -c 'pg_dumpall -U postgres' > /home/santa_crm_test/backup/dump_santa_crm.sql
cp /home/santa_crm_test/backup/dump_santa_crm.sql /home/santa_crm_test/backup/dump_santa_crm_"`date +\%Y-\%m-\%d`".sql
echo "Finished backup `date +\%Y-\%m-\%d`" >> santa_crm_backup.log