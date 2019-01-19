FROM anapsix/alpine-java
LABEL maintainer="laerdh@gmail.com"
ENV SPRING_PROFILES_ACTIVE=prod
COPY ./build/libs/recieppy-api-*.jar /services/recieppy-api.jar
CMD ["java", "-jar", "/services/recieppy-api.jar"]