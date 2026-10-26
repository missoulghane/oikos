interface SelectedFileListProps {
  files: File[];
  onRemove: (index: number) => void;
  disabled?: boolean;
  /**
   * Titre saisi pour chaque fichier, dans le même ordre. Absent : pas de champ
   * titre (l'API reprendra le nom du fichier).
   */
  titles?: string[];
  onTitleChange?: (index: number, title: string) => void;
}

/** « Budget 2026.pdf » -> « Budget 2026 » : le titre que l'API retiendra si rien n'est saisi. */
function defaultDocumentTitle(fileName: string): string {
  const dot = fileName.lastIndexOf('.');
  return dot > 0 ? fileName.slice(0, dot) : fileName;
}

/**
 * Files picked but not sent yet, each with a way out. Shared by the upload
 * field of an existing owner and the picker of a creation form, so that a file
 * waiting to be sent looks the same wherever it waits.
 */
export function SelectedFileList({ files, onRemove, disabled = false, titles, onTitleChange }: SelectedFileListProps) {
  if (files.length === 0) {
    return null;
  }
  return (
    <ul className="flex flex-col gap-2">
      {files.map((file, index) => (
        <li
          key={`${file.name}-${index}`}
          className="flex flex-col gap-1 text-sm text-gray-600 sm:flex-row sm:items-center sm:gap-3 dark:text-gray-400"
        >
          {titles && onTitleChange && (
            // Le titre d'abord, le fichier dessous : c'est le titre qu'on lira
            // ensuite dans la liste. Vide, il reprend le nom du fichier.
            <input
              type="text"
              value={titles[index] ?? ''}
              maxLength={200}
              disabled={disabled}
              onChange={(event) => onTitleChange(index, event.target.value)}
              placeholder={defaultDocumentTitle(file.name)}
              aria-label={`Titre de ${file.name}`}
              className="min-h-10 w-full rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-sm text-gray-800 focus:border-brand-300 focus:outline-none focus:ring-3 focus:ring-brand-500/20 sm:max-w-xs dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
            />
          )}
          <div className="flex min-w-0 flex-1 items-center justify-between gap-2">
            <span className="truncate">{file.name}</span>
            <button
              type="button"
              disabled={disabled}
              onClick={() => onRemove(index)}
              aria-label={`Retirer ${file.name}`}
              className="shrink-0 text-xs text-gray-500 hover:text-gray-700 disabled:opacity-50 dark:text-gray-400 dark:hover:text-gray-200"
            >
              Retirer
            </button>
          </div>
        </li>
      ))}
    </ul>
  );
}
