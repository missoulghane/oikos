import { useRef } from 'react';
import { SelectedFileList } from '@/features/property-mngt/documents/components/SelectedFileList';

interface PendingDocumentsPickerProps {
  files: File[];
  onChange: (files: File[]) => void;
  disabled?: boolean;
  /** Names the file input for assistive tech - there is no visible label, the section heading plays that role. */
  ariaLabel: string;
}

/**
 * Documents chosen for something that does not exist yet.
 *
 * <p>UploadDocumentForm sends straight away, which needs an owner id; a
 * creation form has none until it is submitted. This picker only holds the
 * files, and the page uploads them once the owner is created.
 *
 * <p>A second selection adds to the first rather than replacing it: the files
 * of an assembly rarely sit in one folder, and a native input that silently
 * drops the budget when the quote is added is a trap.
 */
export function PendingDocumentsPicker({
  files,
  onChange,
  disabled = false,
  ariaLabel,
}: PendingDocumentsPickerProps) {
  const inputRef = useRef<HTMLInputElement>(null);

  return (
    <div className="flex flex-col gap-2">
      <input
        ref={inputRef}
        type="file"
        multiple
        disabled={disabled}
        aria-label={ariaLabel}
        onChange={(event) => {
          onChange([...files, ...Array.from(event.target.files ?? [])]);
          // Cleared so that picking the same file again after removing it fires a change.
          if (inputRef.current) {
            inputRef.current.value = '';
          }
        }}
        className="block w-full text-sm text-gray-700 dark:text-gray-300 file:mr-3 file:rounded-lg file:border-0 file:bg-gray-100 dark:file:bg-white/[0.05] file:px-3 file:py-2 file:text-sm file:font-medium file:text-gray-700 dark:file:text-gray-300 hover:file:bg-gray-200 dark:hover:file:bg-white/[0.08]"
      />
      <SelectedFileList
        files={files}
        disabled={disabled}
        onRemove={(index) => onChange(files.filter((_, i) => i !== index))}
      />
    </div>
  );
}
