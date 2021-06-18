FROM openjdk:16.0.1-jdk-oraclelinux8
WORKDIR /home/schoolbot

RUN apt update -y && apt install git maven -y
RUN git clone https://github.com/tykoooo/School-Bot-Remastered.git .
RUN mvn clean compile assembly:single
RUN cp target/*.jar schoolbot.jar

ENTRYPOINT java -server -Xmx10G -Dnogui=true -jar schoolbot.jar
