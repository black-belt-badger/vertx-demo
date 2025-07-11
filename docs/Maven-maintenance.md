# Maven maintenance

## [Back](../README.md)

* build
  ```shell
  ./mvnw clean install
  ```

## Dependency management

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

## Enforcement

* enforce
  ```shell
  ./mvnw enforcer:enforce
  ```
