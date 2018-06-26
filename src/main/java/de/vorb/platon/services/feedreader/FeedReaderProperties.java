package de.vorb.platon.services.feedreader;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties("platon.feed-reader")
class FeedReaderProperties {

    private final List<ImportRule> importRules = new ArrayList<>();

    @Data
    static class ImportRule {
        private URL feedUrl;
    }
}
