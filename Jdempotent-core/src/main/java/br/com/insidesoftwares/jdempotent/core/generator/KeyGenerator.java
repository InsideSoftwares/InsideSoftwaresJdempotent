package br.com.insidesoftwares.jdempotent.core.generator;

import br.com.insidesoftwares.jdempotent.core.model.IdempotencyKey;
import br.com.insidesoftwares.jdempotent.core.model.IdempotentRequestWrapper;

import java.security.MessageDigest;

/**
 *
 */
public interface KeyGenerator {

    /**
     *
     * @param requestObject
     * @param listenerName
     * @param builder
     * @param messageDigest
     * @return
     */
    IdempotencyKey generateIdempotentKey(IdempotentRequestWrapper requestObject, String listenerName, StringBuilder builder, MessageDigest messageDigest);

}
