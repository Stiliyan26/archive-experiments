docker stop nepal_nepal_backend_1 
docker exec -i nepal_nepal_postgres_1 bash -c 'psql -U postgres -c "drop database santa_crm"' 
docker exec -i nepal_nepal_postgres_1 psql -U postgres -c "CREATE DATABASE santa_crm WITH OWNER = santa_crm ENCODING = 'UTF8' TABLESPACE = pg_default LC_COLLATE = 'en_US.UTF-8' LC_CTYPE = 'en_US.UTF-8' CONNECTION LIMIT = -1 TEMPLATE template0;"
cat nepal_backup2024-12-20.sql | docker exec -i nepal_nepal_postgres_1 bash -c 'psql -U santa_crm -d santa_crm' 
docker start nepal_nepal_backend_1 