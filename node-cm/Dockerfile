FROM node:16

WORKDIR /usr/src/app
ENV TZ="Asia/Kolkata"


# Copy package.json and yarn.lock to install dependencies
COPY package.json yarn.lock ./

# Install dependencies
RUN yarn install

# Copy the rest of the application code
COPY . .

VOLUME ["/logs"]
# Start the Node.js application
CMD ["node", "app.js"]
