import type { Ref } from 'react';
import { Controller, type Control, type FieldValues, type Path } from 'react-hook-form';
import {
  QuillEditor,
  DEFAULT_EDITOR_MIN_HEIGHT,
  type QuillEditorHandle,
} from '@/shared/components/RichText/QuillEditor';

interface MessageBodyEditorProps<TFieldValues extends FieldValues> {
  ref?: Ref<QuillEditorHandle>;
  control: Control<TFieldValues>;
  name: Path<TFieldValues>;
  ariaLabel: string;
  placeholder: string;
  disabled?: boolean;
  autoFocus?: boolean;
  error?: string;
  minHeight?: number;
  /** L'éditeur occupe la hauteur disponible plutôt que de s'arrêter à minHeight (voir QuillEditor). */
  fill?: boolean;
}

// The body field's wire format is Quill's own HTML output (see QuillEditor) -
// react-hook-form just carries that string around like it did the plain-text
// <textarea> value this replaced, with no other awareness of what's inside it.
// `ref` (see QuillEditorHandle) is how a caller imperatively clears/prefills
// the editor (on send success, on draft load) - QuillEditor's own doc
// comment explains why that's imperative rather than reactive.
export function MessageBodyEditor<TFieldValues extends FieldValues>({
  ref,
  control,
  name,
  ariaLabel,
  placeholder,
  disabled = false,
  autoFocus = false,
  error,
  minHeight,
  fill = false,
}: MessageBodyEditorProps<TFieldValues>) {
  return (
    <Controller
      control={control}
      name={name}
      render={({ field }) => (
        // En mode `fill`, c'est ce conteneur - l'élément flex du formulaire -
        // qui porte le plancher de hauteur, pas l'éditeur lui-même : posé sur
        // l'éditeur, il le faisait déborder d'un parent déjà rétréci et
        // recouvrir les boutons qui suivent (voir QuillEditor). Ici, le
        // formulaire ne peut plus le rétrécir sous ce plancher : il défile.
        <div
          className={`flex flex-col gap-1 ${fill ? 'flex-1' : ''}`}
          style={fill ? { minHeight: minHeight ?? DEFAULT_EDITOR_MIN_HEIGHT } : undefined}
        >
          <QuillEditor
            ref={ref}
            defaultValue={field.value ?? ''}
            onChange={field.onChange}
            onBlur={field.onBlur}
            placeholder={placeholder}
            disabled={disabled}
            autoFocus={autoFocus}
            minHeight={minHeight}
            fill={fill}
            ariaLabel={ariaLabel}
          />
          {error && <p className="text-sm text-error-500 dark:text-error-400">{error}</p>}
        </div>
      )}
    />
  );
}
