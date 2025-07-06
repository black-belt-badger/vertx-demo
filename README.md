# Vert.x Demo

## Building

### Maven

* build
  ```shell
  ./mvnw clean install
  ```
#### Dependency management

* display dependency updates
  ```shell
  ./mvnw versions:display-dependency-updates
  ```
* use latest versions
  ```shell
  ./mvnw versions:use-latest-versions
  ```
* use latest releases
  ```shell
  ./mvnw versions:use-latest-releases
  ```
* use releases
  ```shell
  ./mvnw versions:use-releases
  ```
* display plugin updates
  ```shell
  ./mvnw versions:display-plugin-updates
  ```
* display property updates
  ```shell
  ./mvnw versions:display-property-updates
  ```
#### Enforcement
* enforce
  ```shell
  ./mvnw enforcer:enforce
  ```

### Docker

* build image
  ```shell
  docker build -t marekdudek/vertx-demo .
  ```
* set tag
  ```shell
  export tag=$(./mvnw org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.version -q -DforceStdout)
  echo ${tag}
  ```
* build and tag image
  ```shell
  docker buildx build --tag marekdudek/vertx-demo:${tag} --sbom=true --provenance=true .
  ```
* tag image
  ```shell
  docker image tag marekdudek/vertx-demo:latest marekdudek/vertx-demo:"${tag}"
  ```
* login
  ```shell
  docker login
  ```
* push image
  ```shell
  docker push marekdudek/vertx-demo --all-tags
  ```


## Deployment

### Docker
* On default HTTP port
  ```shell
  docker run --name vert-xdemo -p 80:8080 -d marekdudek/vertx-demo
  ```
* On other HTTP port
  ```shell
  docker run --name vert-xdemo -p 8080:8080 -d marekdudek/vertx-demo
  ```

### AWS

* AMI image
  * `(SupportedImages) - Docker - Debian 12 x86_64` -
    * best choice for the moment, new versions released often
* Key pair
  * `vertx-demo`
  * (almost) never changed
* Security group
  * newly created
  * during creation allow HTTP and HTTPS
* IMDSv2
  * make required during creation

#### Startup
* with `docker run` command
  ```shell
  docker run --name vert-xdemo -p 80:8080 -d marekdudek/vertx-demo
  ```
* **TODO**: how to
* **TODO**: how to startup on reboot of instance?

## Access

* shell
  ```shell
  ssh -i "vertx-demo.pem" admin@ec2-16-171-238-97.eu-north-1.compute.amazonaws.com
  ```
* web
  * HTTP with public DNS
    * [http://ec2-16-171-238-97.eu-north-1.compute.amazonaws.com/](http://ec2-16-171-238-97.eu-north-1.compute.amazonaws.com/)
  * HTTP with public IPv4 address
    * [http://16.171.238.97/](http://16.171.238.97/)
  * **TODO**: add support for HTTPS
