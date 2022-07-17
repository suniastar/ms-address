FROM openjdk:17 as builder
COPY . /usr/src/project
RUN cd /usr/src/project && ./mvnw package

FROM openjdk:17-slim
WORKDIR /opt
ENV JAVA_OPTS="-Xmx256m -Djava.security.egd=file:/dev/random"
COPY --from=builder /usr/src/project/target/address-*.jar /opt/ms-address.jar
EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -jar /opt/ms-address.jar"]
