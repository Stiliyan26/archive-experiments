psql -U postgres -h localhost -c "drop database santa_db_anvoice"
cat dump_santa_bgmashini_2022-07-15.sql | psql -U postgres -h localhost