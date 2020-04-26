FROM node:12 as frontbuild
COPY --chown=node:node ./wonderfull-front /app
WORKDIR /app
RUN npm i && npm run build

FROM gradle:jdk11 AS backbuild
RUN mkdir /app
COPY --chown=gradle:gradle . /app
#RUN mkdir /app/wonderfull-front
RUN mkdir /app/wonderfull-front/dist
#RUN mkdir /app/wonderfull-back
RUN mkdir /app/wonderfull-back/build
RUN mkdir /app/wonderfull-back/build/resources
RUN mkdir /app/wonderfull-back/build/resources/main
RUN mkdir /app/wonderfull-back/build/resources/main/dist
COPY --from=frontbuild /app/dist/ /app/wonderfull-back/build/resources/main/dist/
#COPY --chown=gradle:gradle . /app
# RUN curl -sL https://deb.nodesource.com/setup_12.x | bash -
# RUN apt-get install -y nodejs
# RUN apt-get install -y build-essential
WORKDIR /app
RUN gradle wonderfull-back:build -x check

#FROM openjdk:11
#FROM balenalib/raspberry-pi-debian-openjdk:latest
FROM bellsoft/liberica-openjdk-debian:11
WORKDIR /app
COPY --from=backbuild /app/wonderfull-back/build/libs/iaww.jar /app/wonderfull-back/application-docker.yaml /app/
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 80
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=90.0", "-jar", "/app/iaww.jar"]
