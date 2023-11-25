package br.com.insidesoftwares.jdempotent.redis.configuration;

import br.com.insidesoftwares.jdempotent.core.aspect.IdempotentAspect;
import br.com.insidesoftwares.jdempotent.core.callback.ErrorConditionalCallback;
import br.com.insidesoftwares.jdempotent.redis.repository.RedisIdempotentRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

/**
 *
 */
@AutoConfiguration
@ConditionalOnProperty(
        prefix="jdempotent", name = {"enable"},
        havingValue = "true",
        matchIfMissing = true)
public class ApplicationConfig {

    private final RedisConfigProperties redisProperties;

    public ApplicationConfig(RedisConfigProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Bean
    @ConditionalOnProperty(
            prefix="jdempotent", name = {"enable"},
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnBean(ErrorConditionalCallback.class)
    public IdempotentAspect getIdempotentAspectOnErrorConditionalCallback(@Qualifier("JdempotentRedisTemplate") RedisTemplate redisTemplate, ErrorConditionalCallback errorConditionalCallback) {
        return new IdempotentAspect(new RedisIdempotentRepository(redisTemplate, redisProperties), errorConditionalCallback);
    }

    @Bean
    public IdempotentAspect getIdempotentAspect(@Qualifier("JdempotentRedisTemplate") RedisTemplate redisTemplate) {
        return new IdempotentAspect(new RedisIdempotentRepository(redisTemplate, redisProperties));
    }

}