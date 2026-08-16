package com.architek.oikos.meeting.application.usecase;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns the rich-text comment into plain-text paragraphs, for the one consumer
 * that cannot take HTML: the convocation PDF.
 *
 * <p>openhtmltopdf is used here without an HTML5 parser (see the pom - only
 * openhtmltopdf-pdfbox), so the template it renders has to be well-formed XML.
 * A rich-text editor's output is not: Quill emits {@code <br>} unclosed, and a
 * single one of those turns a convocation letter into a rendering exception.
 * Interpolating the editor's HTML straight into the template would also hand
 * whoever wrote the comment control over the document's markup.
 *
 * <p>So the letter takes the text and drops the formatting. That is a real
 * loss - bold and links do not survive - and it is the right trade for a
 * document that must render, every time, for every lot. The clients keep the
 * formatting; they sanitize instead of stripping, because they can.
 *
 * <p>Deliberately small and dependency-free rather than a parser: the input is
 * one editor's output, the output feeds {@code th:text} (escaped by Thymeleaf),
 * and nothing downstream interprets what comes out of here as markup.
 */
final class RichTextToParagraphs {

    /** The tags that end a paragraph. Everything else is unwrapped in place. */
    private static final String BLOCK_BOUNDARY = "(?i)</p>|<br\\s*/?>|</li>|</h[1-6]>|</blockquote>|</div>";

    private RichTextToParagraphs() {
    }

    static List<String> convert(String html) {
        if (html == null || html.isBlank()) {
            return List.of();
        }
        List<String> paragraphs = new ArrayList<>();
        for (String block : html.split(BLOCK_BOUNDARY)) {
            String text = unescape(block.replaceAll("(?s)<[^>]*>", "")).trim();
            if (!text.isEmpty()) {
                paragraphs.add(text);
            }
        }
        return List.copyOf(paragraphs);
    }

    /**
     * The entities an editor actually produces. Ampersand LAST, deliberately:
     * unescaping it first would turn "&amp;amp;lt;" into "&amp;lt;" and then into
     * "&lt;", putting markup back into text that had just been stripped of it.
     */
    private static String unescape(String text) {
        return text.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&");
    }
}
