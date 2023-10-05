## 📘 Application.yml

RabbitMQ 4대의 포트를 다 적기보다 Config에서 단순히 포트에 +1, +2를 할거고 host/id/pw는 동일하기 때문에,

RabbitMQ 설정은 1개만 작성해줍니다.

```yaml
server:  
  servlet:  
    encoding:  
      charset: UTF-8  
      force-response: true  
  port: 8100  
  
spring:  
  
  # RabbitMQ 설정
  rabbitmq:  
    host: 1.1.1.1  
    port: 5672  
    username: guest  
    password: guest  
  
  # H2 설정
  h2:  
    console:  
      enabled: true  
      path: /h2  
  datasource:  
    url: jdbc:h2:mem:test  
    username: root  
    password: 1234  
  
  # JPA 설정
  jpa:  
    open-in-view: false  
    hibernate:  
      ddl-auto: create  
    show-sql: false  
    properties:  
      hibernate:  
        format_sql: true  
  
# Logging  
logging:  
  pattern:  
    dateformat: "yyyy-MM-dd HH:mm:ss"  
  level:  
    org:  
      hibernate: info
```