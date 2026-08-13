import { useEffect, useImperativeHandle, useRef, type Ref } from 'react';
import Quill from 'quill';
import 'quill/dist/quill.snow.css';
import '@/features/messaging/components/quillEditor.css';

// Deliberately not one of the "Quill for React" wrapper packages (react-quill
// et al. are unmaintained and fight React 18/19's render model) - Quill is
// instantiated once directly on a plain div ref and treated as a genuinely
// uncontrolled widget after that: typing flows one-way, DOM -> onChange ->
// react-hook-form, and nothing ever flows back in reactively from a `value`
// prop. An earlier version of this component tried to keep Quill "in sync"
// with an incoming `value` prop via a useEffect (diffing against a
// last-emitted ref), which sounds reasonable but is racy in practice: RHF's
// state update from one keystroke's onChange can still be in flight when the
// next keystroke fires, so the effect ends up comparing a stale prop against
// live-typed content and "corrects" the editor mid-word, scrambling it (seen
// under fast typing/paste, and reliably reproduced by userEvent.type in
// tests). Imperative reset via `setHtml` (through the ref) sidesteps the
// whole class of bug: it's only ever called from discrete, known moments
// (a successful send, a draft prefill) rather than reactively on every value
// change.
//
// SECURITY: Quill's own HTML output is not guaranteed safe (CVE-2025-15056,
// "Quill vulnerable to XSS via HTML export", unpatched as of 2.0.3) and the
// backend does zero HTML validation on MessageBody (see oikos-api's
// MessageBody value object - just a capped-length string). This component
// only emits the HTML string; renderMessageBody.tsx is the one place that
// actually re-displays it to other users, and it runs everything through
// DOMPurify before touching the DOM - that sanitize call is the real
// security boundary, not this editor.
export interface QuillEditorHandle {
  /** Imperatively replaces the editor's contents - see the file-level comment
   * for why this is a ref method rather than a reactive `value` prop. */
  setHtml(html: string): void;
}

interface QuillEditorProps {
  ref?: Ref<QuillEditorHandle>;
  /** Only read once, at mount - afterwards Quill owns its own content (see
   * setHtml on the handle for how to change it programmatically later). */
  defaultValue?: string;
  onChange: (html: string) => void;
  onBlur?: () => void;
  placeholder?: string;
  disabled?: boolean;
  minHeight?: number;
  autoFocus?: boolean;
  ariaLabel?: string;
}

const TOOLBAR_OPTIONS = [
  ['bold', 'italic', 'underline', 'strike'],
  ['blockquote'],
  [{ list: 'ordered' }, { list: 'bullet' }],
  ['link'],
  ['clean'],
];

// Quill's empty document is "<p><br></p>", not "" - normalized to an actual
// empty string so react-hook-form's min(1) "message can't be empty" check
// (see sendMessageSchema) keeps working the same way it did for the plain
// <textarea> this replaced.
function readHtml(quill: Quill): string {
  return quill.getText().trim().length === 0 ? '' : quill.root.innerHTML;
}

function setHtml(quill: Quill, html: string) {
  quill.setText('', 'silent');
  if (html) {
    quill.clipboard.dangerouslyPasteHTML(html, 'silent');
  }
}

export function QuillEditor({
  ref,
  defaultValue = '',
  onChange,
  onBlur,
  placeholder,
  disabled = false,
  minHeight = 80,
  autoFocus = false,
  ariaLabel,
}: QuillEditorProps) {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const quillRef = useRef<Quill | null>(null);
  const onChangeRef = useRef(onChange);
  // Refs may only be written outside of render (event handlers, effects) -
  // this keeps the 'text-change' listener (registered once, below) calling
  // whatever the latest `onChange` prop is without having to re-subscribe it
  // on every render.
  useEffect(() => {
    onChangeRef.current = onChange;
  });

  useEffect(() => {
    const wrapper = containerRef.current;
    if (!wrapper) {
      return;
    }
    // Quill doesn't just fill the element it's given - it also inserts the
    // toolbar as a *sibling* before it. Mounting Quill on a plain, imperatively
    // created child (never touched by JSX/React's own reconciliation) rather
    // than directly on the React-rendered wrapper means cleanup can simply
    // wipe wrapper.innerHTML without fighting React over who owns which DOM
    // nodes. Without this, StrictMode's dev-only double-invoke of this effect
    // (mount -> cleanup -> mount again) had no cleanup to undo the first
    // mount's DOM insertions, so the second mount left two toolbars behind.
    const editorRoot = document.createElement('div');
    wrapper.appendChild(editorRoot);
    const quill = new Quill(editorRoot, {
      theme: 'snow',
      placeholder,
      modules: { toolbar: TOOLBAR_OPTIONS },
    });
    quillRef.current = quill;
    if (ariaLabel) {
      quill.root.setAttribute('aria-label', ariaLabel);
    }
    if (defaultValue) {
      setHtml(quill, defaultValue);
    }
    quill.on('text-change', () => onChangeRef.current(readHtml(quill)));
    quill.root.addEventListener('blur', () => onBlur?.());
    if (autoFocus) {
      quill.focus();
    }
    return () => {
      quillRef.current = null;
      wrapper.innerHTML = '';
    };
    // Mount-once: `defaultValue` is intentionally excluded (see its doc
    // comment) - the live instance is only ever updated afterwards via the
    // imperative `setHtml` handle, never by re-running this effect.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    quillRef.current?.enable(!disabled);
  }, [disabled]);

  useImperativeHandle(
    ref,
    () => ({
      setHtml(html: string) {
        const quill = quillRef.current;
        if (!quill) {
          return;
        }
        setHtml(quill, html);
        onChangeRef.current(readHtml(quill));
      },
    }),
    [],
  );

  return <div ref={containerRef} style={{ minHeight }} />;
}
