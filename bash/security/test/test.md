# Security tests

## Command-line tests

### Insecure

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

### Secure

* cURL on 0.0.0.0
  ```shell
  ./test-http-https-secure-0.0.0.0.sh
  ```

* cURL on localhost
  ```shell
  ./test-http-https-secure-localhost.sh
  ```

* cURL on 127.0.0.1
  ```shell
  ./test-http-https-secure-127.0.0.1.sh
  ```

## Browser tests

* 0.0.0.0
  * HTTP
    * [http://0.0.0.0:18080](http://0.0.0.0:18080)
  * HTTPS
    * [https://0.0.0.0:18443](https://0.0.0.0:18443)

* localhost
  * HTTP
    * [http://localhost:18080](http://localhost:18080)
  * HTTPS
    * [https://localhost:18443](https://localhost:18443)

* 127.0.0.1
  * HTTP
    * [http://127.0.0.1:18080](http://127.0.0.1:18080)
  * HTTPS
    * [https://127.0.0.1:18443](https://127.0.0.1:18443)

### on 9rove.com

* [https://9rove.com](https://9rove.com)
* [http://9rove.com](http://9rove.com)
* [https://9rove.com:18443](https://9rove.com:18443)
* [http://9rove.com:18080](http://9rove.com:18080)
