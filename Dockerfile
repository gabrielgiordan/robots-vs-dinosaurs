FROM java:8-alpine

ADD target/uberjar/robots-vs-dinosaurs-standalone.jar app/robots-vs-dinosaurs.jar

ENV PORT="5000"
CMD java -Dport=$PORT -jar /app/robots-vs-dinosaurs.jar