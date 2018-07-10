package de.vorb.platon.web.mvc;

import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.ThreadRepository;
import de.vorb.platon.persistence.jooq.tables.pojos.Comment;
import de.vorb.platon.persistence.jooq.tables.pojos.CommentThread;
import de.vorb.platon.services.feeds.atom.AtomDateTime;
import de.vorb.platon.services.feeds.atom.AtomEntry;
import de.vorb.platon.services.feeds.atom.AtomFeed;
import de.vorb.platon.services.feeds.atom.AtomLink;
import de.vorb.platon.services.feeds.atom.AtomPerson;
import de.vorb.platon.services.feeds.atom.AtomText;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AtomFeedController {

    private final ThreadRepository threadRepository;
    private final CommentRepository commentRepository;

    @Value("${platon.public-self-url}")
    private URI publicSelfUrl;

    @GetMapping(value = "/threads/{threadId}/feed", produces = MediaType.APPLICATION_XML_VALUE)
    @Transactional(readOnly = true)
    public AtomFeed getAtomFeedForThread(@PathVariable("threadId") long threadId) {

        final CommentThread thread = threadRepository.findById(threadId).orElseThrow(RuntimeException::new);
        final List<Comment> comments = commentRepository.findPublicByThreadId(threadId);

        final Instant updated = comments.stream()
                .map(Comment::getLastModificationDate)
                .max(LocalDateTime::compareTo)
                .map(localDateTime -> localDateTime.toInstant(ZoneOffset.UTC))
                .orElseGet(Instant::now);

        final URI selfLink = UriComponentsBuilder.fromUri(publicSelfUrl)
                .path("/threads/{threadId}/feed")
                .build()
                .expand(Collections.singletonMap("threadId", threadId))
                .toUri();

        final URI htmlLink = UriComponentsBuilder.fromUri(publicSelfUrl)
                .path("/threads/{threadId}/comments")
                .build()
                .expand(Collections.singletonMap("threadId", threadId))
                .toUri();

        final List<AtomEntry> entries = comments.stream().map(comment -> {
            final URI commentSelfLink = UriComponentsBuilder.fromUri(publicSelfUrl)
                    .path("/threads/{threadId}/comments/{commentId}")
                    .build()
                    .expand(ImmutableMap.of("threadId", threadId, "commentId", comment.getId()))
                    .toUri();

            final AtomPerson.AtomPersonBuilder authorBuilder = AtomPerson.builder().name(comment.getAuthor());
            if (comment.getUrl() != null) {
                authorBuilder.uri(URI.create(comment.getUrl()));
            }
            return AtomEntry.builder()
                    .id(commentSelfLink.toString())
                    .title(comment.getTextReference())
                    .updated(AtomDateTime.of(comment.getLastModificationDate().toInstant(ZoneOffset.UTC)))
                    .authors(Collections.singletonList(authorBuilder.build()))
                    .content(AtomText.html(comment.getTextHtml()))
                    .links(ImmutableList.of(
                            AtomLink.builder()
                                    .href(commentSelfLink.toString())
                                    .rel("self")
                                    .type("text/html")
                                    .build(),
                            AtomLink.builder()
                                    .href(htmlLink.toString() + "#comment-" + comment.getId())
                                    .rel("alternate")
                                    .type("text/html")
                                    .build()))
                    .build();
        }).collect(Collectors.toList());

        return AtomFeed.builder()
                .id(htmlLink.toString())
                .title(String.format("Comments for “%s”", thread.getTitle()))
                .updated(AtomDateTime.of(updated))
                .links(ImmutableList.of(
                        AtomLink.builder()
                                .href(selfLink.toString())
                                .rel("self")
                                .type(MediaType.APPLICATION_ATOM_XML_VALUE)
                                .build(),
                        AtomLink.builder()
                                .href(htmlLink.toString())
                                .rel("alternate")
                                .type(MediaType.TEXT_HTML_VALUE)
                                .build()))
                .entries(entries)
                .build();
    }

}
