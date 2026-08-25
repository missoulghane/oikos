import type { ReactNode } from 'react';
import DOMPurify from 'dompurify';
import { sanitizeRichText } from '@/shared/components/RichText/sanitizeRichText';
import '@/shared/components/RichText/richTextContent.css';

// Body is HTML on this client (Quill's own output, see QuillEditor/
// MessageBodyEditor) since this feature moved off the plain-text
// "**bold**"/"- bullet" markdown-lite it started with - but oikos-mobile
// still writes that plain-text format (no rich editor there, by design: the
// WYSIWYG toolbar is web-only) and older messages may still carry it too.
// Both are handled below: looksLikeHtml() picks a body apart into one path
// or the other.
const HTML_TAG_PATTERN = /<([a-z][a-z0-9]*)\b[^>]*>/i;

function looksLikeHtml(body: string): boolean {
  return HTML_TAG_PATTERN.test(body);
}

// The sanitize pass moved to shared/components/RichText/sanitizeRichText once a
// second feature started writing rich text (a general meeting's comment). It is
// the same allow-list and the same link hook - and keeping one copy is the point:
// the XSS boundary is not something to maintain twice.

// Legacy/mobile plain-text renderer: "**bold**" spans and "- " bullet lines,
// rendered as React elements (never dangerouslySetInnerHTML) - kept in sync
// with oikos-mobile's renderMessageBody.tsx.
function renderInline(text: string, keyPrefix: string): ReactNode {
  const parts = text.split(/(\*\*[^*]+\*\*)/g).filter((part) => part !== '');
  return parts.map((part, index) =>
    part.startsWith('**') && part.endsWith('**') && part.length > 4 ? (
      <strong key={`${keyPrefix}-${index}`}>{part.slice(2, -2)}</strong>
    ) : (
      <span key={`${keyPrefix}-${index}`}>{part}</span>
    ),
  );
}

function renderPlainTextBody(body: string): ReactNode {
  const lines = body.split('\n');
  const blocks: ReactNode[] = [];
  let textBuffer: string[] = [];
  let bulletBuffer: string[] = [];

  function flushText(key: string) {
    if (textBuffer.length === 0) {
      return;
    }
    blocks.push(
      <p key={key} className="whitespace-pre-wrap break-words">
        {textBuffer.map((line, index) => (
          <span key={index}>
            {renderInline(line, `${key}-${index}`)}
            {index < textBuffer.length - 1 && <br />}
          </span>
        ))}
      </p>,
    );
    textBuffer = [];
  }

  function flushBullets(key: string) {
    if (bulletBuffer.length === 0) {
      return;
    }
    blocks.push(
      <ul key={key} className="list-disc space-y-0.5 pl-5">
        {bulletBuffer.map((line, index) => (
          <li key={index}>{renderInline(line, `${key}-${index}`)}</li>
        ))}
      </ul>,
    );
    bulletBuffer = [];
  }

  lines.forEach((line, index) => {
    if (line.startsWith('- ')) {
      flushText(`p-${index}`);
      bulletBuffer.push(line.slice(2));
    } else {
      flushBullets(`ul-${index}`);
      textBuffer.push(line);
    }
  });
  flushText('p-end');
  flushBullets('ul-end');

  return <>{blocks}</>;
}

export function renderMessageBody(body: string): ReactNode {
  if (looksLikeHtml(body)) {
    return <div className="ql-content" dangerouslySetInnerHTML={{ __html: sanitizeRichText(body) }} />;
  }
  return renderPlainTextBody(body);
}

/** Au-delà, la ligne d'aperçu est de toute façon coupée à l'écran. */
const PREVIEW_MAX_CHARS = 100;

/**
 * L'aperçu du dernier message, tel qu'il s'affiche partout : en clair et
 * borné. Partagé par la ligne de conversation et la cloche des messages du
 * header - celle-ci affichait le corps brut, donc les balises HTML de l'éditeur
 * telles quelles.
 */
export function messagePreviewText(preview: string | null): string | null {
  if (!preview) {
    return null;
  }
  const plain = stripMessageBodyMarkup(preview);
  return plain.length > PREVIEW_MAX_CHARS ? `${plain.slice(0, PREVIEW_MAX_CHARS)}…` : plain;
}

// Preview-only cleanup (conversation list row, see ConversationListItem) -
// reduces either format to plain text so the preview never shows literal
// "**"/"- " markers or raw HTML tags.
export function stripMessageBodyMarkup(text: string): string {
  if (looksLikeHtml(text)) {
    // Block-level tags become a space (not dropped outright) before the
    // sanitize-to-plain-text pass, or adjacent blocks like "<p>Hello</p><p>World</p>"
    // would collapse into the word-mashed "HelloWorld".
    const withBreaks = text.replace(/<\/(p|li|blockquote|div)>|<br\s*\/?>/gi, ' ');
    const plain = DOMPurify.sanitize(withBreaks, { ALLOWED_TAGS: [] });
    return plain.replace(/\s+/g, ' ').trim();
  }
  return text.replace(/\*\*(.+?)\*\*/g, '$1').replace(/^[ \t]*-\s+/gm, '');
}
