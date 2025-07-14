# Config management

* Set tag
  ```shell
  export tag=1.0.11
  ```

## Default configuration

* Up
  ```shell
  docker compose up -d
  ```
* Down
  ```shell
  docker compose down
  ```

## Generated configuration

* Up
  ```shell
  docker compose --project-name dev -f ../bash/dhall/data/compose2/out/compose-dev.yaml up -d
  ```
* Down
  ```shell
  docker compose --project-name dev -f ../bash/dhall/data/compose2/out/compose-dev.yaml down
  ```
