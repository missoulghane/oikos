import { useRef, useState } from 'react';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useUploadDocument } from '@/features/property-mngt/documents/hooks/useUploadDocument';
import type { DocumentOwnerType } from '@/features/property-mngt/documents/types/document.types';

interface UploadDocumentFormProps {
  ownerType: DocumentOwnerType;
  ownerId: string;
}

export function UploadDocumentForm({ ownerType, ownerId }: UploadDocumentFormProps) {
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadErrors, setUploadErrors] = useState<string[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const upload = useUploadDocument(ownerType, ownerId);

  function removeFile(index: number) {
    setSelectedFiles((files) => files.filter((_, i) => i !== index));
  }

  async function handleUpload() {
    if (selectedFiles.length === 0) {
      return;
    }
    setIsUploading(true);
    const errors: string[] = [];
    for (const file of selectedFiles) {
      try {
        await upload.mutateAsync(file);
      } catch (error) {
        errors.push(`${file.name} : ${getErrorMessage(error)}`);
      }
    }
    setUploadErrors(errors);
    setSelectedFiles([]);
    setIsUploading(false);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  }

  return (
    <div className="flex flex-col gap-2">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
        <input
          ref={fileInputRef}
          type="file"
          multiple
          onChange={(event) => {
            setUploadErrors([]);
            setSelectedFiles(Array.from(event.target.files ?? []));
          }}
          className="block w-full text-sm text-gray-700 dark:text-gray-300 file:mr-3 file:rounded-lg file:border-0 file:bg-gray-100 dark:file:bg-white/[0.05] file:px-3 file:py-2 file:text-sm file:font-medium file:text-gray-700 dark:file:text-gray-300 hover:file:bg-gray-200 dark:hover:file:bg-white/[0.08]"
        />
        <Button
          type="button"
          className="shrink-0"
          disabled={selectedFiles.length === 0}
          isLoading={isUploading}
          onClick={handleUpload}
        >
          Téléverser{selectedFiles.length > 1 ? ` (${selectedFiles.length})` : ''}
        </Button>
      </div>

      {selectedFiles.length > 0 && (
        <ul className="flex flex-col gap-1">
          {selectedFiles.map((file, index) => (
            <li
              key={`${file.name}-${index}`}
              className="flex items-center justify-between gap-2 text-sm text-gray-600 dark:text-gray-400"
            >
              <span className="truncate">{file.name}</span>
              <button
                type="button"
                onClick={() => removeFile(index)}
                className="shrink-0 text-xs text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
              >
                Retirer
              </button>
            </li>
          ))}
        </ul>
      )}

      {uploadErrors.map((message) => (
        <Alert key={message} message={message} />
      ))}
    </div>
  );
}
