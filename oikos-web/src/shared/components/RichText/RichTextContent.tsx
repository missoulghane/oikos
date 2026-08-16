import { sanitizeDocumentHtml, sanitizeRichText } from '@/shared/components/RichText/sanitizeRichText';
import '@/shared/components/RichText/richTextContent.css';

interface RichTextContentProps {
  html: string;
  /**
   * `document` widens the allow-list to the headings the API composes (the
   * minutes). Default is the editor's own narrow list - what a person typed.
   */
  variant?: 'editor' | 'document';
  className?: string;
}

/**
 * Displays rich text that someone else wrote.
 *
 * <p>The single component for it, so that `dangerouslySetInnerHTML` appears in
 * one place with a sanitize call already attached rather than at every call
 * site with the pass left as an exercise. That is not hypothetical: the minutes
 * tab rendered its content raw until this component existed.
 */
export function RichTextContent({ html, variant = 'editor', className }: RichTextContentProps) {
  const safe = variant === 'document' ? sanitizeDocumentHtml(html) : sanitizeRichText(html);
  return (
    <div
      className={`ql-content text-gray-800 dark:text-white/90 ${className ?? ''}`}
      dangerouslySetInnerHTML={{ __html: safe }}
    />
  );
}
