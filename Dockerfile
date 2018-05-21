FROM openjdk:8-jre
ARG JAR_FILE
ADD ${JAR_FILE} /usr/share/app.jar

ENTRYPOINT ["/usr/bin/java","-Djava.security.egd=file:/dev/./urandom","-jar","/usr/share/app.jar"]
RUN mkdir /usr/share/cache
RUN mkdir /usr/share/render
