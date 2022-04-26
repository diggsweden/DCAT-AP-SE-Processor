FROM docker.io/adoptopenjdk/openjdk11:jdk-11.0.2.9-slim AS build

RUN apt-get -y clean && apt-get -y update && apt-get -y install maven

RUN mkdir /build
COPY pom.xml /build/
COPY src /build/src/
WORKDIR /build
RUN mvn clean package spring-boot:repackage



FROM docker.io/adoptopenjdk/openjdk11:jre-11.0.11_9-debianslim

COPY --from=build /build/target/dcat-ap-processor-0.0.1-SNAPSHOT.jar /opt/app.jar

RUN apt-get -y clean \
    && apt-get -y update \
    && apt-get -y install --no-install-recommends locales \
    && apt-get -y autoremove --purge \
    && apt-get -y clean \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /apidef \
    && sed -i -e 's/# sv_SE.UTF-8 UTF-8/sv_SE.UTF-8 UTF-8/' /etc/locale.gen \
    && locale-gen

WORKDIR /opt
ENV PORT 8080
EXPOSE 8080
CMD exec java $JAVA_OPTS -jar app.jar

## Set the locale
ENV LANG sv_SE.UTF-8
ENV LANGUAGE sv_SE:sv
ENV LC_ALL sv_SE.UTF-8
