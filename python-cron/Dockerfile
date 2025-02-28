# Use an official Python runtime as the base image
FROM python:3.9-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Python script into the container
COPY MariaDB_To_PG /MariaDB_To_PG

# Install cron and any dependencies (if needed)
RUN apt-get update && apt-get install -y cron && rm -rf /var/lib/apt/lists/*

# Copy the cron job file into the container
COPY cronjob /etc/cron.d/cronjob

# Give execution rights to the cron job file
RUN chmod 0644 /etc/cron.d/cronjob

# Apply the cron job
RUN crontab /etc/cron.d/cronjob

# Create a log file for cron output
RUN touch /var/log/cron.log

# Install Python dependencies (if needed)
# COPY requirements.txt .
# RUN pip install --no-cache-dir -r requirements.txt

# Run cron in the foreground
CMD ["cron", "-f"]