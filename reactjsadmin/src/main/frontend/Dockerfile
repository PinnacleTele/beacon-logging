# Use the official Nginx image as the base image
FROM nginx:alpine

# Copy the build files from your React.js application to the Nginx web root directory
COPY build /usr/share/nginx/html

# Copy the Nginx configuration file (if needed)
# COPY nginx.conf /etc/nginx/nginx.conf

# Expose port 80
EXPOSE 80

# Start Nginx when the container starts
CMD ["nginx", "-g", "daemon off;"]
