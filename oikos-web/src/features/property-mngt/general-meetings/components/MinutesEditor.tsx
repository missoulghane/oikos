import { useState } from 'react';
import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

interface MinutesEditorProps {
  initialContent: string;
  isSaving: boolean;
  isRegenerating: boolean;
  isValidating: boolean;
  error: unknown;
  onSave: (content: string) => void;
  onRegenerate: () => void;
  onValidate: () => void;
}

/**
 * The draft editor, holding the unsaved text.
 *
 * <p>Its own component rather than state inside the tab, so the parent can
 * reset it by remounting it (key on the server content) instead of syncing an
 * effect: regenerating the minutes has to throw the local buffer away, and
 * "reset state when a prop changes" is exactly what a key is for.
 */
export function MinutesEditor({
  initialContent,
  isSaving,
  isRegenerating,
  isValidating,
  error,
  onSave,
  onRegenerate,
  onValidate,
}: MinutesEditorProps) {
  const [draft, setDraft] = useState(initialContent);

  return (
    <>
      <p className="text-sm text-gray-500 dark:text-gray-400">
        Complétez le brouillon : débats, remarques, opposition qu'un copropriétaire demande à faire consigner.
        Valider fige définitivement le texte.
      </p>
      <textarea
        aria-label="Contenu du procès-verbal"
        rows={18}
        value={draft}
        onChange={(event) => setDraft(event.target.value)}
        className="rounded-lg border border-gray-300 dark:border-gray-700 bg-transparent px-3 py-2 font-mono text-sm text-gray-800 dark:text-white/90 shadow-theme-xs focus:border-brand-300 focus:outline-none focus:ring-3 focus:ring-brand-500/20"
      />
      {error != null && <Alert message={getErrorMessage(error)} />}
      <div className="flex flex-wrap gap-3">
        <Button isLoading={isSaving} onClick={() => onSave(draft)}>
          Enregistrer
        </Button>
        <Button variant="secondary" isLoading={isRegenerating} onClick={onRegenerate}>
          Régénérer depuis la séance
        </Button>
        <Button variant="secondary" isLoading={isValidating} onClick={onValidate}>
          Valider (fige le texte)
        </Button>
      </div>
      <p className="text-xs text-gray-400 dark:text-gray-500">
        Régénérer écrase les modifications manuelles et repart des données de la séance.
      </p>
    </>
  );
}
