# Use OpenJDK 21 as base image
FROM openjdk:21-jdk-slim

# Install necessary utilities
RUN apt-get update && apt-get install -y curl unzip

# Download and install WildFly 21.0.0.Final
RUN curl -L https://github.com/wildfly/wildfly/releases/download/21.0.0.Final/wildfly-21.0.0.Final.zip -o /opt/wildfly.zip \
    && unzip /opt/wildfly.zip -d /opt/ \
    && rm /opt/wildfly.zip

# Set WildFly environment variables
ENV WILDFLY_HOME=/opt/wildfly-21.0.0.Final
ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk
ENV PATH=$JAVA_HOME/bin:$WILDFLY_HOME/bin:$PATH
