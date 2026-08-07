import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useInvitations } from '@/features/property-mngt/invitations/hooks/useInvitations';
import { useMembershipRequests } from '@/features/property-mngt/invitations/hooks/useMembershipRequests';
import { InvitationList } from '@/features/property-mngt/invitations/components/InvitationList';
import { MembershipRequestList } from '@/features/property-mngt/invitations/components/MembershipRequestList';
import { CreateInvitationForm } from '@/features/property-mngt/invitations/components/CreateInvitationForm';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function PropertyInvitationsTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const [invitationsPage, setInvitationsPage] = useState(0);
  const [membershipRequestsPage, setMembershipRequestsPage] = useState(0);
  const [isCreating, setIsCreating] = useState(false);

  const invitations = useInvitations(property.id, invitationsPage);
  const membershipRequests = useMembershipRequests(property.id, membershipRequestsPage);

  return (
    <div className="flex flex-col gap-4">
      <Card className="flex flex-col gap-4">
        <div className="flex items-center justify-between gap-2">
          <h2 className="text-base font-semibold text-gray-900">Invitations</h2>
          {!isCreating && (
            <Button type="button" onClick={() => setIsCreating(true)}>
              Nouvelle invitation
            </Button>
          )}
        </div>

        {isCreating && (
          <CreateInvitationForm
            propertyId={property.id}
            onSuccess={() => setIsCreating(false)}
            onCancel={() => setIsCreating(false)}
          />
        )}

        {invitations.isLoading && <Loader label="Chargement des invitations…" />}
        {invitations.isError && <Alert message={getErrorMessage(invitations.error)} />}
        {invitations.data && (
          <InvitationList propertyId={property.id} data={invitations.data} onPageChange={setInvitationsPage} />
        )}
      </Card>

      <Card className="flex flex-col gap-4">
        <h2 className="text-base font-semibold text-gray-900">Demandes d'adhésion</h2>
        {membershipRequests.isLoading && <Loader label="Chargement des demandes d'adhésion…" />}
        {membershipRequests.isError && <Alert message={getErrorMessage(membershipRequests.error)} />}
        {membershipRequests.data && (
          <MembershipRequestList
            propertyId={property.id}
            data={membershipRequests.data}
            onPageChange={setMembershipRequestsPage}
          />
        )}
      </Card>
    </div>
  );
}
