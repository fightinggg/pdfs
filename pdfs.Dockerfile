FROM maven
WORKDIR /app
COPY pom.xml /app/pom.xml
RUN mvn dependency:go-offline
COPY . /app
RUN mvn package -Dmaven.test.skip

FROM maven
COPY --from=0 /app/target/pdfs-0.0.1-SNAPSHOT-jar-with-dependencies.jar /app/pdfs.jar
CMD java -jar /app/pdfs.jar
