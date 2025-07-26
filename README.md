# Vert.x Demo

## [AWS EC2 Setup](docs/AWS-EC2-Setup.md)

## [AWS SSM and CloudWatch  Setup](docs/AWS-SSM-and-CloudWatch-Setup.md)

## [Maven maintenance](docs/Maven-maintenance.md)

## [AMQP](docs/AMQP.md)

## [Redis](docs/Redis.md)

## Building

### Docker

* extract tag
  ```shell
  export tag=$(./mvnw org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.version -q -DforceStdout)
  echo ${tag}
  ```
* build and tag image
  ```shell
  docker buildx build --tag marekdudek/vertx-demo:${tag} --sbom=true --provenance=true .
  ```
* login
  ```shell
  docker login
  ```
* push image
  ```shell
  docker push marekdudek/vertx-demo:${tag}
  ```

## Deployment

### Docker

* On default HTTP port
  ```shell
  docker run --name vert-xdemo -p 80:8080 -d marekdudek/vertx-demo
  ```
* Run with `docker compose` command
  ```shell
  docker compose up -d
  ```
* Stop with `docker compose` command
  ```shell
  docker compose down
  ```
* Pull with `docker compose`
  ```shell
  docker compose pull
  ```

* Remove qpid data volume
  ```shell
  docker volume prune --all --force
  ```

## Access

* shell
  ```shell
  ssh -i "vertx-demo.pem" admin@ec2-13-60-243-123.eu-north-1.compute.amazonaws.com
  ```
* web
  * HTTP with public DNS
    * [http://ec2-13-60-243-123.eu-north-1.compute.amazonaws.com/](http://ec2-13-60-243-123.eu-north-1.compute.amazonaws.com/)
  * HTTP with public IPv4 address
    * [http://13.60.243.123/](http://13.60.243.123/)
  * **TODO**: add support for HTTPS

* debug

  local and remote debug IntelliJ run configurations are stored in `./run` folder

* Vert.x. Shell
  * dev
    ```shell
    telnet -d localhost 5000
    ```
  * prod
    ```shell
    telnet -d 13.60.243.123 5000
    ```

* JMX
  * jconsole
    * dev
     ```shell
     jconsole localhost:1099 &
     ```
    * prod
     ```shell
     jconsole ec2-51-21-163-63.eu-north-1.compute.amazonaws.com &
     ```
  * VisualVM
    * dev
    ```shell
    visualvm --nosplash --openjmx localhost:1099 &
    ```
    * prod
    ```shell
    visualvm --nosplash --openjmx ec2-51-21-163-63.eu-north-1.compute.amazonaws.com:1099 &
    ```
* Docker
  ```shell
  docker exec -it vertx-demo bash
  ```
