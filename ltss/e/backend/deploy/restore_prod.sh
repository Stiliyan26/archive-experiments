docker stop prod_santa_crm_backend_1 
docker exec -i prod_santa_postgres_1 bash -c 'psql -U postgres -c "drop database santa_db_evn"' 
cat /home/santa_crm/backup/dump_santa_crm.sql | docker exec -i prod_santa_postgres_1 bash -c 'psql -U postgres' 
docker start prod_santa_crm_backend_1 