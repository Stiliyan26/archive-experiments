# IMPORTANT NOTE: The dist build (dist dir) has to be compiled/bundled before building the Docker image
# Import node v8.4
FROM node:8.4

# Create app directory
WORKDIR /usr/src/app

COPY package.json .
COPY package-lock.json .

#Install app dependencies
RUN npm install babel-cli

COPY ./dist ./dist
COPY ./src/server.js ./src/server.js
COPY ./.babelrc ./.babelrc

EXPOSE 4000
CMD [ "npm", "run", "dist"]