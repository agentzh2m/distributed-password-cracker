FROM openjdk:8-alpine

COPY target/scala-2.12/cracker_server.jar /app/cracker_server.jar
#specify where the config file is
CMD ["java", "-jar", "/app/cracker_server.jar"]