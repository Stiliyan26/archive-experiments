#https://docs.docker.com/engine/examples/postgresql_service/
#https://hub.docker.com/_/postgres/
# Use a parent image
FROM postgres:9.6


# Copy the current directory contents into the container directory
ADD *.sql /docker-entrypoint-initdb.d/

# Install any needed packages specified in requirements.txt
RUN echo "host all  all    0.0.0.0/0  md5" >> /var/lib/postgresql/data/pg_hba.conf
#RUN echo "listen_addresses='*'" >> /var/lib/postgresql/data/pg_hba.conf

# Add VOLUMEs to allow backup of config, logs and databases
#VOLUME  ["/etc/postgresql", "/var/log/postgresql", "/var/lib/postgresql"]
