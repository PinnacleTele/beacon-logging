# Use the official Node.js runtime as the base image
FROM node:14

# Set the working directory in the container
WORKDIR /app

# Copy the package.json and package-lock.json to the container
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy the rest of the application code to the container
COPY . .

# Build the Angular application (change 'ng build' command if needed)
RUN npm run build

# Expose the port that your Angular app will run on
EXPOSE 81

# Start the Angular application when the container starts
CMD ["npm", "start"]
