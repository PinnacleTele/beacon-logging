#FROM dk-reg.mas:5000/openjdk:21-jdk-slim
#FROM dk-reg.mas:5000/wildfly/wildfly:latest-jdk21

FROM openjdk:21-jdk-slim

RUN mkdir -p /opt/jboss/wildfly/logs

ENV TZ="Asia/Kolkata"
EXPOSE 20000/tcp




COPY ./target/profile/digitalocean3/kafka2es_del.properties_do3 /kafka2es_del.properties_do3
COPY ./target/profile/digitalocean3/kafka2es_sub.properties_do3 /kafka2es_sub.properties_do3
COPY ./target/profile/siddhulog4j/log4j.xml /log4j.xml
COPY ./target/siddhu-k2es-1.0-executable.jar /siddhu-k2es-1.0-executable.jar

#ENTRYPOINT ["java","-Dlog4j.configurationFile=file:/log4j2-common.xml","-jar","/siddhu-k2es-1.0-executable.jar"]

#java \
#-Xms1g -Xmx1g \
#-Dlog4j.configurationFile=file:./cfg/log4j2-kafka2elastic-del-otp.xml \
#-Dkafka.2.elasticsearch.config.file=./cfg/kafka2es_del.properties \
#-jar kafka2elasticsearch-1.0.0-executable.jar $CONSUMER_MODE $TOPIC_NAME $CONSUMER_GROUP_SEQ $THREADS_COUNT > "./log/"$out_fn 2>&1 &