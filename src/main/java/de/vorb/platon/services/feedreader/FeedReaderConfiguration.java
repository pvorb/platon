package de.vorb.platon.services.feedreader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
class FeedReaderConfiguration {

    private final FeedReaderProperties feedReaderProperties;
    private final FeedReader feedReader;
    private final ScheduledExecutorService scheduler;

    public FeedReaderConfiguration(FeedReaderProperties feedReaderProperties, FeedReader feedReader) {
        this.feedReaderProperties = feedReaderProperties;
        this.feedReader = feedReader;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @PostConstruct
    void registerTriggers() {
        feedReaderProperties.getImportRules().forEach(importRule -> {
            try {
                final URI feedUrl = importRule.getFeedUrl().toURI();
                final Runnable importFeedJob = () -> feedReader.importFeed(feedUrl);
                scheduler.scheduleAtFixedRate(importFeedJob, 0, 10, TimeUnit.MINUTES);
            } catch (URISyntaxException e) {
                log.warn("URL '{}' cannot be converted to URI", importRule.getFeedUrl(), e);
            }
        });
    }

    @PreDestroy
    void shutdownScheduler() {
        scheduler.shutdownNow();
    }

}
