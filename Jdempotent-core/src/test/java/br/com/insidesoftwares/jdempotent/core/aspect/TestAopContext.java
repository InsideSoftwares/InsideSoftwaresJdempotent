package br.com.insidesoftwares.jdempotent.core.aspect;

import br.com.insidesoftwares.jdempotent.core.datasource.InMemoryIdempotentRepository;
import br.com.insidesoftwares.jdempotent.core.generator.DefaultKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {"br.com.insidesoftwares.jdempotent.core"})
class TestAopContext {

    @Bean
    IdempotentAspect idempotentAspect(InMemoryIdempotentRepository inMemoryIdempotentRepository, DefaultKeyGenerator defaultKeyGenerator) {
        return new IdempotentAspect(inMemoryIdempotentRepository, defaultKeyGenerator);
    }

    @Bean
    InMemoryIdempotentRepository inMemoryIdempotentRepository() {
        return new InMemoryIdempotentRepository();
    }

    @Bean
    DefaultKeyGenerator defaultKeyGenerator() {
        return new DefaultKeyGenerator();
    }

}
