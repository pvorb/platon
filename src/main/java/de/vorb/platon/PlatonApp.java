package de.vorb.platon;

import de.vorb.platon.security.HmacRequestVerifier;
import de.vorb.platon.security.SecretKeyProvider;
import de.vorb.platon.util.CurrentTimeProvider;
import de.vorb.platon.util.InputSanitizer;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

@SpringBootApplication
public class PlatonApp {

    private static final Logger logger = LoggerFactory.getLogger(PlatonApp.class);

    public static void main(String... args) {
        SpringApplication.run(PlatonApp.class, args);
    }

    private final SecretKey secretKey;

    public PlatonApp() throws NoSuchAlgorithmException {
        secretKey = KeyGenerator.getInstance(HmacRequestVerifier.HMAC_ALGORITHM.toString()).generateKey();

        logger.info("Secret key: {}", Base64.getEncoder().encodeToString(secretKey.getEncoded()));
    }

    @Bean
    public WebMvcConfigurerAdapter staticResourceHandlerConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("classpath:/static");
            }
        };
    }

    @Bean
    public SecretKeyProvider secretKeyProvider() {
        return () -> secretKey;
    }

    @Bean
    public CurrentTimeProvider timeProvider() {
        return () -> Instant.now();
    }

    private static final PolicyFactory HTML_CONTENT_POLICY = new HtmlPolicyBuilder()
            .allowElements(
                    "h1", "h2", "h3", "h4", "h5", "h6",
                    "br", "p", "hr",
                    "div", "span",
                    "a", "img",
                    "em", "strong",
                    "ol", "ul", "li",
                    "blockquote",
                    "code", "pre")
            .allowUrlProtocols("http", "https", "mailto")
            .allowAttributes("href").onElements("a")
            .allowAttributes("src", "width", "height", "alt").onElements("img")
            .allowAttributes("class").onElements("div", "span")
            .toFactory();

    @Bean
    public InputSanitizer htmlInputSanitizer() {
        return HTML_CONTENT_POLICY::sanitize;
    }

}
