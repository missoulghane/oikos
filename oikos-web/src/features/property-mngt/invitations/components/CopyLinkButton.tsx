import { useState } from 'react';
import { Button } from '@/shared/components/Button/Button';
import { CopyIcon, CheckLineIcon } from '@/shared/icons';

const FEEDBACK_DURATION_MS = 2000;

interface CopyLinkButtonProps {
  link: string;
}

export function CopyLinkButton({ link }: CopyLinkButtonProps) {
  const [copied, setCopied] = useState(false);

  async function handleClick() {
    await navigator.clipboard.writeText(link);
    setCopied(true);
    setTimeout(() => setCopied(false), FEEDBACK_DURATION_MS);
  }

  return (
    <Button type="button" variant="secondary" onClick={handleClick}>
      <span className="flex items-center gap-1.5">
        {copied ? <CheckLineIcon className="h-4 w-4 text-success-600" /> : <CopyIcon className="h-4 w-4" />}
        {copied ? 'Lien copié !' : 'Copier le lien'}
      </span>
    </Button>
  );
}
