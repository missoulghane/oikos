import { useState } from 'react';
import { Card } from '@/shared/components/Card/Card';
import { Badge } from '@/shared/components/Badge/Badge';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { Loader } from '@/shared/components/Loader/Loader';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { CopyLinkButton } from '@/features/property-mngt/invitations/components/CopyLinkButton';
import { InvitationLinkCard } from '@/features/property-mngt/invitations/components/InvitationLinkCard';
import { usePublicInvitation } from '@/features/property-mngt/invitations/hooks/usePublicInvitation';
import { useCreateInvitation } from '@/features/property-mngt/invitations/hooks/useCreateInvitation';
import { useDisableInvitation } from '@/features/property-mngt/invitations/hooks/useDisableInvitation';
import { useEnableInvitation } from '@/features/property-mngt/invitations/hooks/useEnableInvitation';

interface PublicInvitationSectionProps {
  propertyId: string;
}

/**
 * Le lien public de la copropriété, à côté de son nom et de son adresse : il
 * en fait partie, au même titre. Il vivait dans un écran « Invitations » où il
 * cohabitait avec les demandes d'adhésion et les invitations nominatives -
 * trois objets sans rapport, et un lien que personne n'allait chercher là.
 *
 * <p>Un seul lien, pour toujours : on le désactive, on le réactive, on n'en
 * crée jamais un second (le serveur le refuse, voir
 * PublicInvitationAlreadyExistsException). Un QR code imprimé dans le hall ou
 * collé sur les avis doit continuer de fonctionner.
 */
export function PublicInvitationSection({ propertyId }: PublicInvitationSectionProps) {
  const publicInvitation = usePublicInvitation(propertyId);
  const createInvitation = useCreateInvitation(propertyId);
  const disableInvitation = useDisableInvitation(propertyId);
  const enableInvitation = useEnableInvitation(propertyId);
  const [showQrCode, setShowQrCode] = useState(false);

  const invitation = publicInvitation.data?.invitation ?? null;
  const isActive = invitation?.status === 'ACTIVE';

  return (
    <Card className="flex flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">Lien public d'adhésion</h2>
          {invitation && (
            <Badge color={isActive ? 'success' : 'light'}>{isActive ? 'Actif' : 'Désactivé'}</Badge>
          )}
        </div>
        {invitation &&
          (isActive ? (
            <Button
              type="button"
              variant="secondary"
              isLoading={disableInvitation.isPending}
              onClick={() => disableInvitation.mutate(invitation.id)}
            >
              Désactiver
            </Button>
          ) : (
            <Button
              type="button"
              isLoading={enableInvitation.isPending}
              onClick={() => enableInvitation.mutate(invitation.id)}
            >
              Réactiver
            </Button>
          ))}
      </div>

      <p className="text-sm text-gray-500 dark:text-gray-400">
        Partagez ce lien ou son QR code avec les copropriétaires : chacun y choisit son lot et dépose une demande
        d'adhésion, que vous validez depuis « Demandes d'adhésion ». Le lien est unique et permanent — désactivez-le
        pour fermer les adhésions, réactivez-le pour les rouvrir, sans jamais invalider un QR code déjà affiché.
      </p>

      {publicInvitation.isLoading && <Loader label="Chargement du lien public…" />}
      {publicInvitation.isError && <Alert message={getErrorMessage(publicInvitation.error)} />}
      {createInvitation.isError && <Alert message={getErrorMessage(createInvitation.error)} />}
      {disableInvitation.isError && <Alert message={getErrorMessage(disableInvitation.error)} />}
      {enableInvitation.isError && <Alert message={getErrorMessage(enableInvitation.error)} />}

      {publicInvitation.data && !invitation && (
        <div>
          <Button
            type="button"
            isLoading={createInvitation.isPending}
            onClick={() => createInvitation.mutate({ propertyId, type: 'PUBLIC' })}
          >
            Créer le lien public
          </Button>
        </div>
      )}

      {invitation && (
        <div className="flex flex-col gap-3">
          {/* Le lien reste lisible et copiable même désactivé : le syndic doit
              pouvoir vérifier de quel lien on parle avant de le rouvrir. */}
          <p className="break-all text-sm text-gray-700 dark:text-gray-300">{invitation.link}</p>
          <p className="text-xs text-gray-400 dark:text-gray-500">
            Valable jusqu'au {new Date(invitation.expiresAt).toLocaleDateString('fr-FR')}
          </p>
          <div className="flex flex-wrap gap-2">
            <CopyLinkButton link={invitation.link} />
            <Button type="button" variant="secondary" onClick={() => setShowQrCode((value) => !value)}>
              {showQrCode ? 'Masquer le QR code' : 'QR code'}
            </Button>
          </div>
          {showQrCode && <InvitationLinkCard link={invitation.link} />}
        </div>
      )}
    </Card>
  );
}
