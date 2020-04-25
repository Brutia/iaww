FROM node:12 as frontbuild
COPY --chown=node:node ./wonderfull-front /app
WORKDIR /app
RUN npm i && npm run build

FROM gradle:jdk11 AS backbuild
COPY --chown=gradle:gradle . /app
RUN mkdir /app /app/wonderfull-front/dist /app/wonderfull-back/build /app/wonderfull-back/build/resources /app/wonderfull-back/build/resources/main /app/wonderfull-back/build/resources/main/dist
COPY --from=frontbuild /app/dist/ /app/wonderfull-back/build/resources/main/dist/
WORKDIR /app
RUN gradle wonderfull-back:build -x check

FROM bellsoft/liberica-openjdk-debian:11
WORKDIR /app
COPY --from=backbuild /app/wonderfull-back/build/libs/iaww.jar /app/wonderfull-back/application-docker.yaml /app/
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 80
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=90.0", "-jar", "/app/iaww.jar"]
