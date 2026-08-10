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
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const upload = useUploadDocument(ownerType, ownerId);

  function handleUpload() {
    if (!selectedFile) {
      return;
    }
    upload.mutate(selectedFile, {
      onSuccess: () => {
        setSelectedFile(null);
        if (fileInputRef.current) {
          fileInputRef.current.value = '';
        }
      },
    });
  }

  return (
    <div className="flex flex-col gap-2">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
        <input
          ref={fileInputRef}
          type="file"
          onChange={(event) => setSelectedFile(event.target.files?.[0] ?? null)}
          className="block w-full text-sm text-gray-700 file:mr-3 file:rounded-lg file:border-0 file:bg-gray-100 file:px-3 file:py-2 file:text-sm file:font-medium file:text-gray-700 hover:file:bg-gray-200"
        />
        <Button
          type="button"
          className="shrink-0"
          disabled={!selectedFile}
          isLoading={upload.isPending}
          onClick={handleUpload}
        >
          Téléverser
        </Button>
      </div>
      {upload.isError && <Alert message={getErrorMessage(upload.error)} />}
    </div>
  );
}
