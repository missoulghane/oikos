import { describe, expect, it } from 'vitest';
import { sanitizeDocumentHtml, sanitizeRichText } from '@/shared/components/RichText/sanitizeRichText';

/**
 * The product's XSS boundary for every rich-text field, so its behaviour is
 * pinned rather than assumed. Two fields feed it - a message body and a general
 * meeting's comment - and the API validates the HTML of neither.
 */
describe('sanitizeRichText', () => {
  it('keeps what the editor toolbar can produce', () => {
    const html = '<p><strong>Gras</strong> et <em>italique</em></p><ul><li>Point</li></ul>';

    expect(sanitizeRichText(html)).toContain('<strong>Gras</strong>');
    expect(sanitizeRichText(html)).toContain('<li>Point</li>');
  });

  it('strips a script tag entirely', () => {
    expect(sanitizeRichText('<p>Bonjour</p><script>alert(1)</script>')).not.toContain('script');
  });

  it('strips an inline event handler while keeping the text', () => {
    const cleaned = sanitizeRichText('<p onclick="alert(1)">Cliquez</p>');

    expect(cleaned).not.toContain('onclick');
    expect(cleaned).toContain('Cliquez');
  });

  it('drops a javascript: link', () => {
    expect(sanitizeRichText('<a href="javascript:alert(1)">Piège</a>')).not.toContain('javascript:');
  });

  it('opens surviving links away from the app', () => {
    // A comment is written by one person and read by every copropriétaire, so a link in it
    // must not be able to drive the tab it was opened from.
    const cleaned = sanitizeRichText('<a href="https://example.com">Devis</a>');

    expect(cleaned).toContain('rel="noopener noreferrer"');
    expect(cleaned).toContain('target="_blank"');
  });

  it('removes the headings a typed comment has no business carrying', () => {
    expect(sanitizeRichText('<h1>Titre</h1><p>Texte</p>')).not.toContain('<h1>');
  });
});

describe('sanitizeDocumentHtml', () => {
  it('keeps the headings the minutes composer emits', () => {
    // The reason this second allow-list exists: running the editor's narrow one over the
    // minutes would strip every heading and leave the document unreadable.
    const cleaned = sanitizeDocumentHtml('<h1>Procès-verbal</h1><h2>Présences</h2><p>12 lots</p>');

    expect(cleaned).toContain('<h1>Procès-verbal</h1>');
    expect(cleaned).toContain('<h2>Présences</h2>');
  });

  it('is still a sanitize pass, not a passthrough', () => {
    // The syndic edits the minutes draft by hand before publishing, so the string is
    // user input whatever composed it first.
    expect(sanitizeDocumentHtml('<h2>Titre</h2><script>alert(1)</script>')).not.toContain('script');
    expect(sanitizeDocumentHtml('<h2 onmouseover="alert(1)">Titre</h2>')).not.toContain('onmouseover');
  });
});
