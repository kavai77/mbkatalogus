FROM openjdk:8-alpine
ARG JAR_FILE
ADD ${JAR_FILE} /usr/share/app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/usr/share/app.jar"]
RUN mkdir /usr/share/cache
RUN mkdir /usr/share/render
RUN mkdir /usr/share/cikkek
RUN mkdir /usr/share/logok
RUN mkdir /usr/share/db
