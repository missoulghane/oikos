import type { Ref } from 'react';
import { Controller, type Control, type FieldValues, type Path } from 'react-hook-form';
import { QuillEditor, type QuillEditorHandle } from '@/shared/components/RichText/QuillEditor';

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
}: MessageBodyEditorProps<TFieldValues>) {
  return (
    <Controller
      control={control}
      name={name}
      render={({ field }) => (
        <div className="flex flex-col gap-1">
          <QuillEditor
            ref={ref}
            defaultValue={field.value ?? ''}
            onChange={field.onChange}
            onBlur={field.onBlur}
            placeholder={placeholder}
            disabled={disabled}
            autoFocus={autoFocus}
            minHeight={minHeight}
            ariaLabel={ariaLabel}
          />
          {error && <p className="text-sm text-error-500 dark:text-error-400">{error}</p>}
        </div>
      )}
    />
  );
}
