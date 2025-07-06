FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /opt/app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ["./mvnw", "dependency:go-offline"]
COPY ./src ./src
RUN ["./mvnw", "clean", "install", "-DskipTests"]

FROM eclipse-temurin:21-jre-jammy AS final
WORKDIR /opt/app
EXPOSE 8080
COPY --from=builder /opt/app/target/vertx-demo-*-fat.jar /opt/app/vertx-demo.jar
RUN ["groupadd", "group"]
RUN ["useradd", "-m", "-g", "group", "user"]
USER user
ENTRYPOINT ["java", "-jar", "/opt/app/vertx-demo.jar"]
CMD ["bbb.vertx_demo.MainVerticle"]
