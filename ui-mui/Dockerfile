# Use an official Node.js runtime as a parent image
FROM node:14 AS build

# Set the working directory in the container
WORKDIR /app

# Copy package.json and package-lock.json to the container
COPY package*.json ./

# Install project dependencies
RUN npm install

# Copy the rest of the application code
COPY . .

# Build the Angular application
RUN npm run build --configuration=production
RUN cp -rp /app/dist/* /app/.

# Use a lightweight web server to serve the Angular app
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY --from=build /app/dist/myapp /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

