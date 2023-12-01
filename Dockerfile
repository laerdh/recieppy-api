FROM eclipse-temurin:17-jdk-jammy
LABEL maintainer="ledahl.com"
COPY ./build/libs/recieppy-api-*.jar /services/recieppy-api.jar
CMD ["java", "-jar", "/services/recieppy-api.jar"]