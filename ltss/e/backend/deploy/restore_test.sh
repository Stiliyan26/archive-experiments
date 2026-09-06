docker stop test_santa_crm_backend_1 
docker exec -i test_santa_postgres_1 bash -c 'psql -U postgres -c "drop database santa_crm"' 
cat /home/santa_crm_test/backup/dump_santa_crm.sql | docker exec -i test_santa_postgres_1 bash -c 'psql -U postgres' 
docker start test_santa_crm_backend_1 