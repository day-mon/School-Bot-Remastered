FROM maven:3.8.1-adoptopenjdk-16 as build
WORKDIR /home/schoolbot

COPY . .
RUN mvn clean compile assembly:single

FROM adoptopenjdk/openjdk16-openj9:alpine
WORKDIR /home/schoolbot

COPY --from=build /home/schoolbot/target/*.jar schoolbot.jar

ENTRYPOINT java -server -Xmx10G -Dnogui=true -jar schoolbot.jar