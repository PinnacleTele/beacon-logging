FROM adoptopenjdk/openjdk11:ubi
ENV TZ="Asia/Kolkata"
EXPOSE 20000/tcp


COPY ./target/platform-chcore-1.0-executable.jar /platform-chcore-1.0-executable.jar
ENTRYPOINT ["java","-Dprometheus.jetty.port=20000","-Dcommon.property.file.location=/instance/global.properties","-Dlog4j.configurationFile=file:/instance/log4j2-common.xml","-jar","/platform-chcore-1.0-executable.jar"]
VOLUME ["/instance"]
