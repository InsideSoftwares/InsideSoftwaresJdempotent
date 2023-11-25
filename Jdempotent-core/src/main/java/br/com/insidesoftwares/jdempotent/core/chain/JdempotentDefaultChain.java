package br.com.insidesoftwares.jdempotent.core.chain;

import br.com.insidesoftwares.jdempotent.core.model.ChainData;
import br.com.insidesoftwares.jdempotent.core.model.KeyValuePair;

import java.lang.reflect.Field;

public class JdempotentDefaultChain extends AnnotationChain {

    @Override
    public KeyValuePair process(ChainData chainData) throws IllegalAccessException {
        Field declaredField = chainData.getDeclaredField();
        declaredField.setAccessible(true);
        return new KeyValuePair(declaredField.getName(),declaredField.get(chainData.getArgs()));
    }
}
