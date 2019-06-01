FROM java:8-alpine

ADD target/robots-vs-dinosaurs-0.1.0-SNAPSHOT-standalone.jar /robots-vs-dinosaurs/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/robots-vs-dinosaurs/app.jar"]