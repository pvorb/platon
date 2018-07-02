package de.vorb.platon.web.mvc;

import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.ThreadRepository;
import de.vorb.platon.persistence.jooq.tables.pojos.Comment;
import de.vorb.platon.persistence.jooq.tables.pojos.CommentThread;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AtomFeedController {

    private static final String ATOM_1_0 = "atom_1.0";

    private final ThreadRepository threadRepository;
    private final CommentRepository commentRepository;

    @GetMapping(value = "/threads/{threadId}/feed")
    public void getAtomFeedForThread(@PathVariable("threadId") long threadId, HttpServletResponse response)
            throws IOException, FeedException {

        response.setContentType(MediaType.APPLICATION_ATOM_XML_VALUE);

        final SyndFeed atomFeed = new SyndFeedImpl();
        atomFeed.setFeedType(ATOM_1_0);

        final CommentThread thread = threadRepository.findById(threadId).orElseThrow(RuntimeException::new);
        final List<Comment> comments = commentRepository.findPublicByThreadId(threadId);

        atomFeed.setTitle(thread.getTitle());
        atomFeed.setLink(thread.getUrl());
        comments.stream().map(Comment::getLastModificationDate).max(LocalDateTime::compareTo).ifPresent(maxDateTime -> {
            atomFeed.setPublishedDate(Date.from(maxDateTime.toInstant(ZoneOffset.UTC)));
        });

        final List<SyndEntry> entries = comments.stream()
                .map(comment -> {
                    final SyndEntry entry = new SyndEntryImpl();
                    entry.setTitle(comment.getTextReference());
                    final SyndContent content = new SyndContentImpl();
                    content.setType("text/html");
                    content.setValue(comment.getTextHtml());
                    entry.setContents(Collections.singletonList(content));
                    entry.setAuthor(comment.getAuthor());
                    entry.setLink("/threads/" + threadId + "/comments#comment-" + comment.getId());
                    entry.setPublishedDate(Date.from(comment.getLastModificationDate().toInstant(ZoneOffset.UTC)));
                    return entry;
                })
                .collect(Collectors.toList());

        atomFeed.setEntries(entries);

        final SyndFeedOutput output = new SyndFeedOutput();
        try (final OutputStreamWriter outputWriter =
                     new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
            output.output(atomFeed, outputWriter);
        }
    }
}
