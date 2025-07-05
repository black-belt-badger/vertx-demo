# Vert.x Demo

## Building

* Set up
   ```shell
  export NAMESPACE=marekdudek
  export NAME=vertx-demo
   ```
* Build image
  ```shell
  docker build -t "${NAMESPACE}"/"${NAME}" .
  ```
* Login
  ```shell
  docker login
  ```
* Publish image
  ```shell
  docker push "${NAMESPACE}"/"${NAME}"
  ```
