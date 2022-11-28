# base image of java runtime (version 18)
FROM openjdk:18

# add maintainer info
LABEL maintainer="Rajat Kumar"

WORKDIR /tmp/hw

COPY target/scala-3.2.1/CS441-Homework3-*.jar /simulations.jar

CMD ["java", "-jar", "/simulations.jar"]

EXPOSE 8080