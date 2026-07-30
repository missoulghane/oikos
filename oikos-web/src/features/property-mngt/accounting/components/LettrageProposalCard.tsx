import { useUnitLettrageProposal } from '@/features/property-mngt/accounting/hooks/useUnitLettrageProposal';
import { useValidateUnitLettrage } from '@/features/property-mngt/accounting/hooks/useValidateUnitLettrage';
import { Card } from '@/shared/components/Card/Card';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

interface LettrageProposalCardProps {
  propertyId: string;
  unitId: string;
}

export function LettrageProposalCard({ propertyId, unitId }: LettrageProposalCardProps) {
  const proposal = useUnitLettrageProposal(propertyId, unitId);
  const { mutate, isPending, error } = useValidateUnitLettrage(propertyId, unitId);

  if (!proposal.data || proposal.data.proposedLines.length === 0) {
    return null;
  }

  const nbEcheances = new Set(proposal.data.proposedLines.map((line) => line.debitMovementId)).size;

  return (
    <Card className="flex flex-col gap-3 border-brand-300">
      {error && <Alert message={getErrorMessage(error)} />}
      <p className="text-sm text-gray-700">
        <span className="font-semibold">{proposal.data.totalProposedAmount.toLocaleString('fr-FR')} MAD</span> en
        attente d'affectation - {nbEcheances} échéance(s) vont être soldée(s) si tu valides le lettrage.
      </p>
      <Button type="button" onClick={() => mutate()} isLoading={isPending} className="self-start">
        Valider le lettrage
      </Button>
    </Card>
  );
}
