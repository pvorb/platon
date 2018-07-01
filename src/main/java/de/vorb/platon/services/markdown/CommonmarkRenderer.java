package de.vorb.platon.services.markdown;

import lombok.RequiredArgsConstructor;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

@RequiredArgsConstructor
public class CommonmarkRenderer implements MarkdownRenderer {

    private final Parser parser;
    private final HtmlRenderer htmlRenderer;

    @Override
    public String renderToHtml(String markdown) {
        final Node parsedMarkdown = parser.parse(markdown);
        return htmlRenderer.render(parsedMarkdown);
    }
}
