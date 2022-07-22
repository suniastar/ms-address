FROM openjdk:17-slim
WORKDIR /opt
ENV JAVA_OPTS="-Xmx256m -Djava.security.egd=file:/dev/random"
COPY ./target/address-*.jar /opt/ms-address.jar
EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -jar /opt/ms-address.jar"]
