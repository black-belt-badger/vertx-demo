# Security tests

## Command-line tests

* cURL on 0.0.0.0
  ```shell
  ./test-http-https-insecure-0.0.0.0.sh
  ```

* cURL on localhost
  ```shell
  ./test-http-https-insecure-localhost.sh
  ```

* cURL on 127.0.0.1
  ```shell
  ./test-http-https-insecure-127.0.0.1.sh
  ```

## Browser tests

* 0.0.0.0
  * HTTP
    * [http://0.0.0.0:8080](http://0.0.0.0:8080)
  * HTTPS
    * [https://0.0.0.0:8443](https://0.0.0.0:8443)

* localhost
  * HTTP
    * [http://localhost:8080](http://localhost:8080)
  * HTTPS
    * [https://localhost:8443](https://localhost:8443)

* 127.0.0.1
  * HTTP
    * [http://127.0.0.1:8080](http://127.0.0.1:8080)
  * HTTPS
    * [https://127.0.0.1:8443](https://127.0.0.1:8443)
