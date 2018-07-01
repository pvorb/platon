package de.vorb.platon.web.mvc;

import com.google.common.collect.ImmutableList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private static final List<Locale> SUPPORTED_LOCALES = ImmutableList.of(Locale.forLanguageTag("en-US"));

    @Bean
    public LocaleResolver localeResolver() {
        final AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setSupportedLocales(SUPPORTED_LOCALES);
        return localeResolver;
    }

}
