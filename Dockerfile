FROM adoptopenjdk/openjdk11:jdk-11.0.2.9-slim
WORKDIR /opt
ENV PORT 8080
EXPOSE 8080
COPY target/*.jar /opt/app.jar
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
RUN mkdir -p /apidef

# Set the locale
RUN apt-get clean && apt-get update && apt-get install -y locales
RUN sed -i -e 's/# sv_SE.UTF-8 UTF-8/sv_SE.UTF-8 UTF-8/' /etc/locale.gen && locale-gen
ENV LANG sv_SE.UTF-8
ENV LANGUAGE sv_SE:sv
ENV LC_ALL sv_SE.UTF-8
