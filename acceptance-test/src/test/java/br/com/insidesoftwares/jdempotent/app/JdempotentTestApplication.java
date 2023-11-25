package br.com.insidesoftwares.jdempotent.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@SpringBootConfiguration
@SpringBootApplication
@ComponentScan({"*ignore*", "br.com.insidesoftwares"})
public class JdempotentTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(JdempotentTestApplication.class, args);
    }

}
