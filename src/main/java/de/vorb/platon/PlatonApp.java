package de.vorb.platon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
public class PlatonApp {

    public static void main(String... args) {
        SpringApplication.run(PlatonApp.class, args);
    }

    @Bean
    public WebMvcConfigurerAdapter staticResourceHandlerConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("classpath:/public");
            }
        };
    }

}
