package br.com.insidesoftwares.jdempotent.core.callback;

import br.com.insidesoftwares.jdempotent.core.aspect.IdempotentAspect;
import br.com.insidesoftwares.jdempotent.core.datasource.InMemoryIdempotentRepository;
import br.com.insidesoftwares.jdempotent.core.generator.DefaultKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = { "br.com.insidesoftwares.jdempotent.core" })
public class TestAopWithErrorCallbackContext {

    @Bean
    public IdempotentAspect idempotentAspect (InMemoryIdempotentRepository inMemoryIdempotentRepository, DefaultKeyGenerator defaultKeyGenerator, TestCustomErrorCallback testCustomErrorCallback) {
        return new IdempotentAspect(inMemoryIdempotentRepository,testCustomErrorCallback, defaultKeyGenerator);
    }

    @Bean
    public InMemoryIdempotentRepository inMemoryIdempotentRepository(){
        return new InMemoryIdempotentRepository();
    }

    @Bean
    public DefaultKeyGenerator defaultKeyGenerator(){
        return new DefaultKeyGenerator();
    }

    @Bean
    public TestCustomErrorCallback testCustomErrorCallback(){
        return new TestCustomErrorCallback();
    }

}
