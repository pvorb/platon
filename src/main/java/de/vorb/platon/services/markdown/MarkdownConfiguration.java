package de.vorb.platon.services.markdown;

import com.google.common.collect.ImmutableSet;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.Heading;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.ListBlock;
import org.commonmark.node.ThematicBreak;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MarkdownConfiguration {

    @Bean
    public Extension autolinkExtension() {
        return AutolinkExtension.create();
    }

    @Bean
    public Extension headingAnchorExtension() {
        return HeadingAnchorExtension.builder()
                .idPrefix("comment-text-")
                .build();
    }

    @Bean
    public Extension strikethroughExtension() {
        return StrikethroughExtension.create();
    }

    @Bean
    @ConditionalOnProperty("platon.markdown.gfm-tables.enabled")
    public Extension tablesExtension() {
        return TablesExtension.create();
    }

    @Bean
    public Parser markdownParser(List<Extension> commonmarkExtensions) {
        return Parser.builder()
                .enabledBlockTypes(ImmutableSet.of(
                        Heading.class,
                        ListBlock.class,
                        BlockQuote.class,
                        IndentedCodeBlock.class,
                        FencedCodeBlock.class,
                        ThematicBreak.class,
                        TableBlock.class))
                .extensions(commonmarkExtensions)
                .build();
    }

    @Bean
    public HtmlRenderer markdownHtmlRenderer(List<Extension> commonmarkExtensions) {
        return HtmlRenderer.builder()
                .extensions(commonmarkExtensions)
                .build();
    }

    @Bean
    public MarkdownRenderer commonmarkRenderer(Parser parser, HtmlRenderer htmlRenderer) {
        return new CommonmarkRenderer(parser, htmlRenderer);
    }

}
