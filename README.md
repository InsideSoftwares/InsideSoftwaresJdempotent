# Jdempotent

<p align="center">
  <img src="etc/logo.jpg">
</p>

This project is a copy of the Jdempotent Project
which you can find on GitHub: [Jdempotent](https://github.com/Trendyol/Jdempotent)
- [memojja](https://github.com/memojja) Mehmet ARI - creator

The modification made to this project in relation to the Original:
- Java 21 
- Spring Boot 3.2.0
- Removing couchbase support
- And general dependency update

# Goal of this Jdempotent

Make your endpoints idempotent easily

# Usage

1. First of all, you need to add a dependency to pom.xml

For Redis:

```xml
<dependency>
    <groupId>br.com.insidesoftwares.jdempotent</groupId>
    <artifactId>redis</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. You should add `@JdempotentResource` annotation to the method that you want to make idempotent resource, listener etc.

```java
@GetMapping("/prime-number")
@ResponseStatus(HttpStatus.OK)
@JdempotentResource(cachePrefix = "PrimeNumber.generatePrimeNumber", ttl = 30, ttlTimeUnit = TimeUnit.SECONDS)
public PrimeNumberResponse generatePrimeNumber(
        @RequestParam(required = false, defaultValue = "5", name = "quantityPrimeNumbers") long qtd
){
        return primeNumberService.generatePrimeNumber(qtd);
}

@PostMapping("/send-email-header-body")
@ResponseStatus(HttpStatus.ACCEPTED)
@JdempotentResource(cachePrefix = "MailController.sendEmail")
public SendEmailResponse sendEmailWithHeaderAndBody(
        @JdempotentRequestPayload @RequestHeader("x-idempotency-key") String idempotencyKey,
        @JdempotentRequestPayload @RequestBody SendEmailRequest request
) {
    if (StringUtils.isEmpty(request.getEmail())) {
        throw new InvalidEmailAddressException();
    }

    try {
        mailSenderService.sendMail(request);
    } catch (Exception e) {
        log.debug("MailSenderService.sendEmail() throw exception: {} request: {} ", e, request);
    }

    return new SendEmailResponse("We will send your message");
}
```

If want that idempotencyId in your payload. Put `@JdempotentId` annotation that places the generated idempotency identifier into annotated field.
Can be thought of as @Id annotation in jpa.

```java
public class IdempotentPaylaod {
   @JdempotentId
   private String jdempotentId;
   private Object data;
}
```

You might want to handle the name of the field differently to ensure idempotency. Just use @JdempotentProperty annotation needs to get the field name differently and generate the hash inspired by jackson (@JsonProperty annotation)

```java
public class IdempotentPaylaod {
   @JdempotentProperty("userId")
   private String customerId;
   private Object data;
}
```


3. If you want to handle a custom error case, you need to implement `ErrorConditionalCallback` like the following example:

```java
@Component
public class AspectConditionalCallback implements ErrorConditionalCallback {

    @Override
    public boolean onErrorCondition(Object response) {
        return response == IdempotentStateEnum.ERROR;
    }
    
    public RuntimeException onErrorCustomException() {
        return new RuntimeException("Status cannot be error");
    }

}
```

4. Let's make the configuration:

For redis configuration:

```yaml
jdempotent:
  enable: true
  cache:
    redis:
      database: 1
      password: "password"
      sentinelHostList: 192.168.0.1,192.168.0.2,192.168.0.3
      sentinelPort: "26379"
      sentinelMasterName: "admin"
      expirationTimeHour: 2
      dialTimeoutSecond: 3
      readTimeoutSecond: 3
      writeTimeoutSecond: 3
      maxRetryCount: 3
      expireTimeoutHour: 3
```

Please note that you can disable Jdempotent easily if you need to. 
For example, assume that you don't have a circut breaker and your Redis is down.
In that case, you can disable Jdempotent with the following configuration:

```yaml
  enable: false
```

```java
@SpringBootApplication(
      exclude = { RedisAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class }
)
```

## Licence

[MIT Licence](https://opensource.org/licenses/MIT) <br/>

## Contributing

1. Fork it ( https://github.com/InsideSoftwares/InsideSoftwaresJdempotent/fork )
2. Create your feature branch (git checkout -b my-new-feature)
3. Commit your changes (git commit -am 'Add some feature')
4. Push to the branch (git push origin my-new-feature)
5. Create a new Pull Request

## Contributors

- [memojja](https://github.com/memojja) Mehmet ARI - creator
- [sawcunha](https://github.com/sawcunha) Samuel Cunha - maintainer
