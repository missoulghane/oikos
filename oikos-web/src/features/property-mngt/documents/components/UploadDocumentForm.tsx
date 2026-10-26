import { useRef, useState } from 'react';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { ProgressBar } from '@/shared/components/ProgressBar/ProgressBar';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useUploadDocument } from '@/features/property-mngt/documents/hooks/useUploadDocument';
import { SelectedFileList } from '@/features/property-mngt/documents/components/SelectedFileList';
import type { DocumentOwnerType } from '@/features/property-mngt/documents/types/document.types';

interface UploadDocumentFormProps {
  ownerType: DocumentOwnerType;
  ownerId: string;
}

interface UploadProgress {
  fileName: string;
  /** Rang du fichier en cours (à partir de 1) sur le nombre total envoyé. */
  position: number;
  total: number;
  percent: number;
}

export function UploadDocumentForm({ ownerType, ownerId }: UploadDocumentFormProps) {
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
  const [titles, setTitles] = useState<string[]>([]);
  const [progress, setProgress] = useState<UploadProgress | null>(null);
  const [uploadErrors, setUploadErrors] = useState<string[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const upload = useUploadDocument(ownerType, ownerId);
  const isUploading = progress !== null;

  function removeFile(index: number) {
    setSelectedFiles((files) => files.filter((_, i) => i !== index));
    setTitles((current) => current.filter((_, i) => i !== index));
  }

  function changeTitle(index: number, title: string) {
    setTitles((current) => current.map((value, i) => (i === index ? title : value)));
  }

  async function handleUpload() {
    if (selectedFiles.length === 0) {
      return;
    }
    const errors: string[] = [];
    for (const [index, file] of selectedFiles.entries()) {
      const position = index + 1;
      setProgress({ fileName: file.name, position, total: selectedFiles.length, percent: 0 });
      try {
        await upload.mutateAsync({
          file,
          title: titles[index],
          onProgress: (percent) =>
            setProgress({ fileName: file.name, position, total: selectedFiles.length, percent }),
        });
      } catch (error) {
        errors.push(`${file.name} : ${getErrorMessage(error)}`);
      }
    }
    setUploadErrors(errors);
    setSelectedFiles([]);
    setTitles([]);
    setProgress(null);
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
          disabled={isUploading}
          aria-label="Choisir des documents"
          onChange={(event) => {
            const files = Array.from(event.target.files ?? []);
            setUploadErrors([]);
            setSelectedFiles(files);
            setTitles(files.map(() => ''));
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
          Ajouter{selectedFiles.length > 1 ? ` (${selectedFiles.length})` : ''}
        </Button>
      </div>

      <SelectedFileList
        files={selectedFiles}
        onRemove={removeFile}
        disabled={isUploading}
        titles={titles}
        onTitleChange={changeTitle}
      />

      {progress && (
        <ProgressBar
          value={progress.percent}
          label={
            progress.total > 1
              ? `Envoi de ${progress.fileName} (${progress.position}/${progress.total})`
              : `Envoi de ${progress.fileName}`
          }
        />
      )}

      {uploadErrors.map((message) => (
        <Alert key={message} message={message} />
      ))}
    </div>
  );
}
