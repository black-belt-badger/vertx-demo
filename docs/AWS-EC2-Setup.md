# AWS EC2 Setup

## [Back](../README.md)

* AMI image
  * `debian-12-amd64-20250316-2053`
* Key pair
  * `vertx-demo`
  * (almost) never changed
* Security group
  * newly created
  * during creation allow HTTP and HTTPS
  * during creation allow inbound on 5005 if You want remote debug
* IMDSv2
  * make required during creation
* Ports to open:
  * SSH, open by default
  * HTTP, suggested by launcher
  * HTTPS, suggested by launcher
  * JDWP - 5005
  * Vert.x Shell - 5000

## Startup

* with `docker compose`
  ```shell
  docker compose up -d
  ```
  * **file `compose.yaml` on *AWS* *EC2* instance is different then the one commited in repo!**
  * **so is `./logs/logback.xml`!**
* **TODO**: how to startup on reboot of instance?
