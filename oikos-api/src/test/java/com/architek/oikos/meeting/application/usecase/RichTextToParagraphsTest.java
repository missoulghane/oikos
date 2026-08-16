package com.architek.oikos.meeting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * The one place the meeting module flattens rich text, and the only one that
 * feeds the convocation PDF - a document that has to render for every lot,
 * every time. openhtmltopdf runs here without an HTML5 parser, so a single
 * unclosed tag from the editor is a rendering exception rather than a cosmetic
 * defect; that is what these assertions stand in front of.
 */
class RichTextToParagraphsTest {

    @Test
    void an_absent_comment_produces_no_paragraph() {
        assertThat(RichTextToParagraphs.convert(null)).isEmpty();
        assertThat(RichTextToParagraphs.convert("")).isEmpty();
        assertThat(RichTextToParagraphs.convert("   ")).isEmpty();
        // Quill's "empty" document is not an empty string.
        assertThat(RichTextToParagraphs.convert("<p><br></p>")).isEmpty();
    }

    @Test
    void each_block_becomes_its_own_paragraph() {
        assertThat(RichTextToParagraphs.convert("<p>Premier point.</p><p>Second point.</p>"))
                .containsExactly("Premier point.", "Second point.");
    }

    @Test
    void a_line_break_splits_as_a_block_does() {
        // <br> is exactly the tag that would break the PDF if it were interpolated raw.
        assertThat(RichTextToParagraphs.convert("<p>Une ligne<br>et une autre</p>"))
                .containsExactly("Une ligne", "et une autre");
        assertThat(RichTextToParagraphs.convert("<p>Une ligne<br/>et une autre</p>"))
                .containsExactly("Une ligne", "et une autre");
    }

    @Test
    void list_items_survive_as_paragraphs() {
        assertThat(RichTextToParagraphs.convert("<ul><li>Budget</li><li>Ravalement</li></ul>"))
                .containsExactly("Budget", "Ravalement");
    }

    @Test
    void inline_formatting_is_unwrapped_and_its_text_kept() {
        assertThat(RichTextToParagraphs.convert("<p>Le <strong>budget</strong> est <em>joint</em>.</p>"))
                .containsExactly("Le budget est joint.");
    }

    @Test
    void a_link_keeps_its_label_and_loses_its_markup() {
        assertThat(RichTextToParagraphs.convert("<p>Voir <a href=\"https://example.com\">le devis</a>.</p>"))
                .containsExactly("Voir le devis.");
    }

    @Test
    void entities_come_back_as_the_characters_they_stand_for() {
        assertThat(RichTextToParagraphs.convert("<p>Charges &amp; travaux &gt; 10 000 &euro;</p>"))
                .containsExactly("Charges & travaux > 10 000 &euro;");
    }

    @Test
    void an_escaped_entity_does_not_come_back_as_markup() {
        // The double-unescape trap: unescaping & before &lt; would turn "&amp;lt;script&amp;gt;"
        // into "<script>", putting markup back into text that had just been stripped of it.
        assertThat(RichTextToParagraphs.convert("<p>&amp;lt;script&amp;gt;</p>"))
                .containsExactly("&lt;script&gt;");
    }

    @Test
    void a_script_tag_leaves_nothing_executable_behind() {
        // Not the security boundary - Thymeleaf's th:text is, and the clients sanitize - but
        // the letter must not carry the text of a script either.
        assertThat(RichTextToParagraphs.convert("<p>Bonjour</p><script>alert(1)</script>"))
                .containsExactly("Bonjour", "alert(1)");
    }

    @Test
    void whitespace_only_blocks_are_dropped_rather_than_printed_blank() {
        assertThat(RichTextToParagraphs.convert("<p>Texte</p><p>   </p><p>&nbsp;</p><p>Suite</p>"))
                .containsExactly("Texte", "Suite");
    }
}
