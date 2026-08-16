import DOMPurify from 'dompurify';

/**
 * SECURITY: the one place rich-text HTML is cleaned before it reaches the DOM.
 *
 * Two fields feed it today - a message body and a general meeting's comment -
 * and the API validates the HTML of neither: both are capped-length strings on
 * the server (oikos-api's MessageBody value object, GeneralMeeting.comment).
 * Quill's own export is not guaranteed safe either (CVE-2025-15056, "Quill
 * vulnerable to XSS via HTML export", unpatched as of quill@2.0.3). So this
 * call is the actual XSS boundary, and it has to run on every string before
 * any dangerouslySetInnerHTML - whichever client, or direct API call, produced
 * it.
 *
 * The allow-list matches exactly what QuillEditor's toolbar can produce.
 * Nothing else has a legitimate reason to be in one of these fields.
 */
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

export function sanitizeRichText(html: string): string {
  ensureSafeLinkHook();
  return DOMPurify.sanitize(html, SANITIZE_CONFIG);
}

/**
 * The wider allow-list, for HTML the API composes rather than a user types:
 * the minutes (MinutesComposer emits h1/h2/h3 on top of the tags above).
 *
 * Still sanitized, and not merely as a formality - the minutes are a draft the
 * syndic edits by hand before publication, so the string that comes back is
 * user input whatever produced it first. Separate from sanitizeRichText because
 * running the editor's narrow allow-list over them would strip every heading
 * and leave the document unreadable.
 */
const DOCUMENT_SANITIZE_CONFIG = {
  ALLOWED_TAGS: [...SANITIZE_CONFIG.ALLOWED_TAGS, 'h1', 'h2', 'h3', 'h4', 'hr'],
  ALLOWED_ATTR: SANITIZE_CONFIG.ALLOWED_ATTR,
};

export function sanitizeDocumentHtml(html: string): string {
  ensureSafeLinkHook();
  return DOMPurify.sanitize(html, DOCUMENT_SANITIZE_CONFIG);
}
