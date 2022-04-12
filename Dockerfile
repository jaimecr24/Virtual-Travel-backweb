FROM openjdk:11
COPY ./backweb/target/backweb-0.0.1-SNAPSHOT.jar /usr/local/lib/backweb.jar
ENTRYPOINT ["java","-jar","/usr/local/lib/backweb.jar"]