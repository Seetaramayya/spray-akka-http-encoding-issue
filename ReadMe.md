# AkkaHttp Spray Encoding issue

### Reproduction steps

- Storing the data using akka http which stores the content as `application/json`
- When spray client reading the data, it is decoding data as `ISO-8859-1` instead of `UTF-8`
- So added `charset` param to fix the problem 
  
  ```
   ContentType(MediaTypes.`application/json`.withParams(Map("charset" -> "UTF-8")))
  ```