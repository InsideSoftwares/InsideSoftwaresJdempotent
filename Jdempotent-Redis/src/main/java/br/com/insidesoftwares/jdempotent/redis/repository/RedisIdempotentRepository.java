package br.com.insidesoftwares.jdempotent.redis.repository;

import br.com.insidesoftwares.jdempotent.core.datasource.IdempotentRepository;
import br.com.insidesoftwares.jdempotent.core.model.IdempotencyKey;
import br.com.insidesoftwares.jdempotent.core.model.IdempotentRequestResponseWrapper;
import br.com.insidesoftwares.jdempotent.core.model.IdempotentRequestWrapper;
import br.com.insidesoftwares.jdempotent.core.model.IdempotentResponseWrapper;
import br.com.insidesoftwares.jdempotent.redis.configuration.RedisConfigProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

/**
 *
 * An implementation of the idempotent IdempotentRepository
 * that uses a distributed hash map from Redis
 *
 * That repository needs to store idempotent hash for idempotency check
 *
 */
public class RedisIdempotentRepository implements IdempotentRepository {

    private final ValueOperations<String, IdempotentRequestResponseWrapper> valueOperations;
    private final RedisTemplate redisTemplate;
    private final RedisConfigProperties redisProperties;


    public RedisIdempotentRepository(@Qualifier("JdempotentRedisTemplate") RedisTemplate redisTemplate, RedisConfigProperties redisProperties) {
        valueOperations = redisTemplate.opsForValue();
        this.redisTemplate = redisTemplate;
        this.redisProperties = redisProperties;
    }

    @Override
    public boolean contains(IdempotencyKey idempotencyKey) {
        return valueOperations.get(idempotencyKey.getKeyValue()) != null;
    }

    @Override
    public IdempotentResponseWrapper getResponse(IdempotencyKey idempotencyKey) {
        return valueOperations.get(idempotencyKey.getKeyValue()).getResponse();
    }

    @Override
    public void store(IdempotencyKey idempotencyKey, IdempotentRequestWrapper request, Long ttl, TimeUnit timeUnit) {
        ttl = ttl == 0 ? redisProperties.getExpirationTimeHour() : ttl;
        valueOperations.set(idempotencyKey.getKeyValue(), prepareValue(request), ttl, timeUnit);
    }

    @Override
    public void remove(IdempotencyKey idempotencyKey) {
        redisTemplate.delete(idempotencyKey.getKeyValue());
    }

    /**
     * ttl describe
     *
     * @param idempotencyKey
     * @param request
     * @param response
     * @param ttl
     */
    @Override
    public void setResponse(IdempotencyKey idempotencyKey, IdempotentRequestWrapper request, IdempotentResponseWrapper response, Long ttl, TimeUnit timeUnit) {
        if (contains(idempotencyKey)) {
            ttl = ttl == 0 ? redisProperties.getExpirationTimeHour() : ttl;
            IdempotentRequestResponseWrapper requestResponseWrapper = valueOperations.get(idempotencyKey.getKeyValue());
            requestResponseWrapper.setResponse(response);
            valueOperations.set(idempotencyKey.getKeyValue(), prepareValue(request, response), ttl, timeUnit);
        }
    }

    /**
     * Prepares the value stored in redis
     *
     * if persistReqRes set to false,
     * it does not persist related request values in redis
     * @param request
     * @return
     */
    private IdempotentRequestResponseWrapper prepareValue(IdempotentRequestWrapper request) {
        if (redisProperties.getPersistReqRes()) {
            return new IdempotentRequestResponseWrapper(request);
        }
        return new IdempotentRequestResponseWrapper(null);
    }

    /**
     * Prepares the value stored in redis
     *
     * if persistReqRes set to false,
     * it does not persist related request and response values in redis
     * @param request
     * @param response
     * @return
     */
    private IdempotentRequestResponseWrapper prepareValue(IdempotentRequestWrapper request, IdempotentResponseWrapper response) {
        if (redisProperties.getPersistReqRes()) {
            return new IdempotentRequestResponseWrapper(request, response);
        }
        return new IdempotentRequestResponseWrapper(null);
    }
}

