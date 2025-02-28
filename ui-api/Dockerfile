FROM node:16

WORKDIR /usr/src/app

# Copy package.json and yarn.lock to install dependencies
COPY src/main/node/package.json src/main/node/yarn.lock ./

# Install dependencies
RUN yarn install

# Copy the rest of the application code
COPY src/main/node/ .

RUN npm install


VOLUME ["/logs"]
# Start the Node.js application
CMD ["node", "app.js"]
