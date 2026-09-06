# Use a parent image
FROM openjdk:11

# Set the working directory
WORKDIR /santa

# Copy the current directory contents into the container directory
ADD . /santa

USER root

COPY santa_crm.crt $JAVA_HOME/lib/security

RUN cd $JAVA_HOME/lib/security && keytool -keystore cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias santa_crm -file santa_crm.crt

# Install any needed packages specified in requirements.txt
#RUN pip install --trusted-host pypi.python.org -r requirements.txt

# Make port available to the world outside this container
EXPOSE 8443

# Define environment variable
#ENV NAME World

# Run app.py when the container launches
CMD ["java", "-jar", "santa_anvoice.jar"]