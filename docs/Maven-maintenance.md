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
  cd .. && ./mvnw enforcer:enforce
  ```
## Versions

* next snapshot (from release)
  ```shell
  cd .. && ./mvnw versions:set -DnextSnapshot=true
  ```
* clean up versions backup file
  ```shell
  cd .. && ./mvnw versions:commit
  ```
* revert version
  ```shell
  cd .. && ./mvnw versions:revert
  ```
