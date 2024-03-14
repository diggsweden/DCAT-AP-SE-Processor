FROM docker.io/library/openjdk:17-jdk-slim AS build

RUN mkdir /build
COPY pom.xml /build/
COPY src /build/src/
COPY .mvn /build/.mvn
COPY mvnw /build/mvnw
WORKDIR /build
RUN ./mvnw clean package spring-boot:repackage



# Note: The default non root chainguard user is 65532
FROM cgr.dev/chainguard/jdk:latest

USER root
RUN mkdir -p /opt/.logs \
    && mkdir -p /apidef
RUN chown -R 65532:65532 /opt/
USER 65532
COPY --from=build /build/target/dcat-ap-processor-0.0.2-SNAPSHOT.jar /opt/app.jar


ENV JDK_JAVA_OPTIONS -Duser.language=sv-SE -Duser.region=SE -Duser.timezone=Europe/Stockholm
WORKDIR /opt
USER root
RUN chmod +x app.jar
USER 65532
ENV PORT 8080
EXPOSE 8080
CMD ["java","-jar","app.jar"]
