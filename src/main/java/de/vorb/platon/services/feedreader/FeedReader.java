package de.vorb.platon.services.feedreader;

import de.vorb.platon.persistence.ThreadRepository;
import de.vorb.platon.persistence.jooq.tables.pojos.CommentThread;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndLink;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedReader {

    private static final String REL_ALTERNATE = "alternate";

    private final ThreadRepository threadRepository;

    @Transactional
    void importFeed(URI feedUrl) {

        try (final CloseableHttpClient client = HttpClients.createMinimal()) {

            final HttpUriRequest request = new HttpGet(feedUrl);

            try (final CloseableHttpResponse response = client.execute(request);
                 final InputStream content = response.getEntity().getContent()) {

                final SyndFeed feed = parseFeed(content);

                feed.getEntries().forEach(this::handleEntry);

            } catch (FeedException e) {
                log.info("Feed at {} is in invalid format", feedUrl);
            }
        } catch (IOException e) {
            log.debug("Could not GET feed at {}", feedUrl);
        }
    }

    private SyndFeed parseFeed(InputStream content) throws FeedException, IOException {
        final SyndFeedInput syndFeedInput = new SyndFeedInput();
        return syndFeedInput.build(new XmlReader(content));
    }

    private void handleEntry(SyndEntry feedEntry) {

        final List<SyndLink> entryLinks = feedEntry.getLinks();

        if (entryLinks != null && !entryLinks.isEmpty()) {

            final Stream<SyndLink> alternateLinks = entryLinks.stream()
                    .filter(link -> REL_ALTERNATE.equalsIgnoreCase(link.getRel()));

            alternateLinks.findFirst().ifPresent(entryLink -> {
                final String threadUrl = entryLink.getHref();
                final Optional<CommentThread> existingThread = threadRepository.findThreadForUrl(threadUrl);
                if (!existingThread.isPresent()) {
                    threadRepository.insert(new CommentThread().setTitle(feedEntry.getTitle()).setUrl(threadUrl));
                    log.debug("Inserted new thread for URL {}", threadUrl);
                } else {
                    if (!existingThread.get().getTitle().equals(feedEntry.getTitle())) {
                        threadRepository.updateThreadTitle(existingThread.get().getId(), feedEntry.getTitle());
                    }
                }
            });
        }

    }

}
