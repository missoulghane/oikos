import type { ReactNode } from 'react';
import DOMPurify from 'dompurify';
import '@/features/messaging/components/messageBodyContent.css';

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

// SECURITY: the backend does zero HTML validation on MessageBody - it's just
// a capped-length string (see oikos-api's MessageBody value object) - and
// Quill's own HTML export isn't guaranteed safe either (CVE-2025-15056,
// unpatched as of quill@2.0.3). This sanitize call is the actual XSS
// boundary: it runs on every body before it ever reaches
// dangerouslySetInnerHTML, regardless of which client (or a direct API call)
// produced the string. The allow-list matches exactly what QuillEditor's
// toolbar can produce - nothing else has a legitimate reason to be here.
const SANITIZE_CONFIG = {
  ALLOWED_TAGS: ['p', 'br', 'strong', 'em', 'u', 's', 'blockquote', 'ol', 'ul', 'li', 'a'],
  ALLOWED_ATTR: ['href'],
};

let linkHookInstalled = false;
function ensureSafeLinkHook() {
  if (linkHookInstalled) {
    return;
  }
  linkHookInstalled = true;
  DOMPurify.addHook('afterSanitizeAttributes', (node) => {
    if (node.tagName === 'A') {
      node.setAttribute('target', '_blank');
      node.setAttribute('rel', 'noopener noreferrer');
    }
  });
}

function sanitizeBodyHtml(body: string): string {
  ensureSafeLinkHook();
  return DOMPurify.sanitize(body, SANITIZE_CONFIG);
}

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
    return <div className="ql-content" dangerouslySetInnerHTML={{ __html: sanitizeBodyHtml(body) }} />;
  }
  return renderPlainTextBody(body);
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
