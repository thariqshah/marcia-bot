FROM openjdk:17-jdk-alpine
MAINTAINER thariqshah
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]