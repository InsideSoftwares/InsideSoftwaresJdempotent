package br.com.insidesoftwares.jdempotent.core.chain;

import br.com.insidesoftwares.jdempotent.core.annotation.JdempotentIgnore;
import br.com.insidesoftwares.jdempotent.core.model.ChainData;
import br.com.insidesoftwares.jdempotent.core.model.KeyValuePair;

import java.lang.reflect.Field;

public class JdempotentIgnoreAnnotationChain extends AnnotationChain {
    @Override
    public KeyValuePair process(ChainData chainData) throws IllegalAccessException {
        Field declaredField = chainData.getDeclaredField();
        declaredField.setAccessible(true);
        JdempotentIgnore annotation = declaredField.getAnnotation(JdempotentIgnore.class);
        if(annotation != null){
            return new KeyValuePair();
        }
        return super.nextChain.process(chainData);
    }
}
