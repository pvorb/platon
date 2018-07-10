package de.vorb.platon.view;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class FreemarkerConfiguration {

    private final freemarker.template.Configuration configuration;

    @PostConstruct
    private void addSharedVariables() {
        configuration.setSharedVariable("base64Url", Base64UrlMethod.INSTANCE);
        configuration.setSharedVariable("byteArrayEquals", ByteArrayEqualsMethod.INSTANCE);
    }

}
