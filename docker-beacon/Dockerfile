#FROM dk-reg.mas:5000/openjdk:21-jdk-slim
#FROM dk-reg.mas:5000/wildfly/wildfly:latest-jdk21

FROM openjdk:21-jdk-slim

RUN mkdir -p /opt/jboss/wildfly/logs

ENV TZ="Asia/Kolkata"
EXPOSE 20000/tcp


COPY ./target/common/log4j2-kafka2elastic.xml /log4j2-kafka2elastic.xml
COPY ./target/common/log4j2-common.xml /log4j2-common.xml
COPY ./target/common/custom_hosts_production.properties /custom_hosts_production.properties
COPY ./target/common/smpp-interface.properties /smpp-interface.properties
COPY ./target/common/encrypt.properties /encrypt.properties
COPY ./target/common/dnd.properties /dnd.properties
COPY ./target/common/common-k2es.properties /common-k2es.properties
COPY ./target/common/dlttemplateconfig.properties /dlttemplateconfig.properties
COPY ./target/common/common.properties /common.properties
COPY ./target/common/callback-params.properties /callback-params.properties
COPY ./target/common/payload-params.properties /payload-params.properties
COPY ./target/common/midconfig.properties /midconfig.properties
COPY ./target/common/messageremove.properties /messageremove.properties
COPY ./target/common/kafka-custom.properties /kafka-custom.properties
COPY ./target/common/jndi.properties /jndi.properties
COPY ./target/common/jettyserver.properties /jettyserver.properties

COPY ./target/common/internal-ui.properties /internal-ui.properties
COPY ./target/common/inmem-refresh-interval.properties /inmem-refresh-interval.properties
COPY ./target/common/http-connector.properties /http-connector.properties
COPY ./target/common/global.properties /global.properties
COPY ./target/common/genericapi.properties /genericapi.properties
COPY ./target/common/exception-case.properties /exception-case.properties
COPY ./target/common/wallet-history.properties /wallet-history.properties
COPY ./target/common/wallet-balance-reminder.properties /wallet-balance-reminder.properties
COPY ./target/common/url_hitter.properties /url_hitter.properties
COPY ./target/common/timezone.properties /timezone.properties
COPY ./target/common/timer-procesor-interval.properties /timer-procesor-interval.properties
COPY ./target/common/sms_request.xsd /sms_request.xsd
COPY ./target/common/shortcode-provider.properties /shortcode-provider.properties
COPY ./target/common/replace-iso.properties /replace-iso.properties
COPY ./target/common/r3c_addinfo.properties /r3c_addinfo.properties
COPY ./target/common/prometheus.control.properties /prometheus.control.properties
COPY ./target/common/mcc-mnc-list.json /mcc-mnc-list.json

COPY ./target/profile/default/kafka2elasticsearch.properties /kafka2elasticsearch.properties_default
COPY ./target/profile/default/kafka-producer.properties /kafka-producer.properties_default
COPY ./target/profile/default/kafka-consumer.properties /kafka-consumer.properties_default
COPY ./target/profile/default/common-db.properties /common-db.properties_default
COPY ./target/profile/default/elasticsearch.properties /elasticsearch.properties_default
COPY ./target/profile/default/kafka2es.properties /kafka2es.properties


COPY ./target/profile/cat/kafka-producer.properties_cat /kafka-producer.properties_cat
COPY ./target/profile/cat/kafka-consumer.properties_cat /kafka-consumer.properties_cat
COPY ./target/profile/cat/common-db.properties_cat /common-db.properties_cat
COPY ./target/profile/cat/elasticsearch.properties_cat /elasticsearch.properties_cat




COPY ./target/profile/horse/kafka-producer.properties_horse /kafka-producer.properties_horse
COPY ./target/profile/horse/kafka-consumer.properties_horse /kafka-consumer.properties_horse
COPY ./target/profile/horse/common-db.properties_horse /common-db.properties_horse
COPY ./target/profile/horse/elasticsearch.properties_horse /elasticsearch.properties_horse



COPY ./target/profile/digitalocean1/kafka-producer.properties_do1 /kafka-producer.properties_do1
COPY ./target/profile/digitalocean1/kafka-consumer.properties_do1 /kafka-consumer.properties_do1
COPY ./target/profile/digitalocean1/common-db.properties_do1 /common-db.properties_do1
COPY ./target/profile/digitalocean1/elasticsearch.properties_do1 /elasticsearch.properties_do1
COPY ./target/profile/digitalocean1/req_receiver.properties_do1 /req_receiver.properties_do1



COPY ./target/profile/digitalocean2/kafka-producer.properties_do2 /kafka-producer.properties_do2
COPY ./target/profile/digitalocean2/kafka-consumer.properties_do2 /kafka-consumer.properties_do2
COPY ./target/profile/digitalocean2/common-db.properties_do2 /common-db.properties_do2
COPY ./target/profile/digitalocean2/elasticsearch.properties_do2 /elasticsearch.properties_do2
COPY ./target/profile/digitalocean2/req_receiver.properties_do2 /req_receiver.properties_do2



COPY ./target/profile/digitalocean3/kafka-producer.properties_do3 /kafka-producer.properties_do3
COPY ./target/profile/digitalocean3/kafka-consumer.properties_do3 /kafka-consumer.properties_do3
COPY ./target/profile/digitalocean3/common-db.properties_do3 /common-db.properties_do3
COPY ./target/profile/digitalocean3/elasticsearch.properties_do3 /elasticsearch.properties_do3
COPY ./target/profile/digitalocean3/req_receiver.properties_do3 /req_receiver.properties_do3

COPY ./target/mainmodule-1.0-executable.jar /mainmodule-1.0-executable.jar

ENTRYPOINT ["java","-Dprometheus.jetty.port=20000","-Dcommon.property.file.location=/global.properties","-Dkafka.2.elasticsearch.config.file=/kafka2es.properties","-Dlog4j.configurationFile=file:/log4j2-common.xml","-jar","/mainmodule-1.0-executable.jar"]

