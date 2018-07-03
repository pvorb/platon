package de.vorb.platon.web.mvc;

import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.ThreadRepository;
import de.vorb.platon.persistence.jooq.tables.pojos.Comment;
import de.vorb.platon.persistence.jooq.tables.pojos.CommentThread;
import de.vorb.platon.services.feeds.atom.AtomEntry;
import de.vorb.platon.services.feeds.atom.AtomFeed;
import de.vorb.platon.services.feeds.atom.AtomLink;
import de.vorb.platon.services.feeds.atom.AtomPerson;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AtomFeedController {

    private static final String ATOM_1_0 = "atom_1.0";

    private final ThreadRepository threadRepository;
    private final CommentRepository commentRepository;

    @Value("${platon.public-self-url}")
    private URI publicSelfUrl;

    @GetMapping(value = "/threads/{threadId}/feed", produces = MediaType.APPLICATION_XML_VALUE)
    public AtomFeed getAtomFeedForThread(@PathVariable("threadId") long threadId) {

        final CommentThread thread = threadRepository.findById(threadId).orElseThrow(RuntimeException::new);
        final List<Comment> comments = commentRepository.findPublicByThreadId(threadId);

        final URI selfLink = UriComponentsBuilder.fromUri(publicSelfUrl)
                .path("/threads/{threadId}/feed")
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
                    .authors(Collections.singletonList(authorBuilder.build()))
                    .content(comment.getTextHtml())
                    .build();
        }).collect(Collectors.toList());

        return AtomFeed.builder()
                .id(selfLink.toString())
                .title(String.format("Comments for “%s”", thread.getTitle()))
                .links(Collections.singletonList(
                        AtomLink.builder().href(selfLink.toString()).rel("self").build()))
                .entries(entries)
                .build();
    }

//    public void getAtomFeedForThread(@PathVariable("threadId") long threadId, HttpServletResponse response)
//            throws IOException, FeedException {
//
//        response.setContentType(MediaType.APPLICATION_ATOM_XML_VALUE);
//
//        final SyndFeed atomFeed = new SyndFeedImpl();
//        atomFeed.setFeedType(ATOM_1_0);
//
//        final CommentThread thread = threadRepository.findById(threadId).orElseThrow(RuntimeException::new);
//        final List<Comment> comments = commentRepository.findPublicByThreadId(threadId);
//
//        atomFeed.setTitle(thread.getTitle());
//        atomFeed.setLink(thread.getUrl());
//        comments.stream().map(Comment::getLastModificationDate).max(LocalDateTime::compareTo).ifPresent(maxDateTime
// -> {
//            atomFeed.setPublishedDate(Date.from(maxDateTime.toInstant(ZoneOffset.UTC)));
//        });
//
//        final List<SyndEntry> entries = comments.stream()
//                .map(comment -> {
//                    final SyndEntry entry = new SyndEntryImpl();
//                    entry.setTitle(comment.getTextReference());
//                    final SyndContent content = new SyndContentImpl();
//                    content.setType("text/html");
//                    content.setValue(comment.getTextHtml());
//                    entry.setContents(Collections.singletonList(content));
//                    entry.setAuthor(comment.getAuthor());
//                    entry.setLink("/threads/" + threadId + "/comments#comment-" + comment.getId());
//                    entry.setPublishedDate(Date.from(comment.getLastModificationDate().toInstant(ZoneOffset.UTC)));
//                    return entry;
//                })
//                .collect(Collectors.toList());
//
//        atomFeed.setEntries(entries);
//
//        final SyndFeedOutput output = new SyndFeedOutput();
//        try (final OutputStreamWriter outputWriter =
//                     new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
//            output.output(atomFeed, outputWriter);
//        }
//    }
}
