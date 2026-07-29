import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { Card } from '@/shared/components/Card/Card';
import { Button } from '@/shared/components/Button/Button';
import { EditPropertyInfoForm } from '@/features/property-mngt/properties/components/EditPropertyInfoForm';
import { AddPropertyManagerForm } from '@/features/property-mngt/properties/components/AddPropertyManagerForm';
import { useCurrentUser, isAdminTierOnProperty } from '@/features/identity/me';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function PropertyGeneralInfoTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const [isEditing, setIsEditing] = useState(false);
  const currentUser = useCurrentUser();
  const canInviteMembers = currentUser.data ? isAdminTierOnProperty(currentUser.data, property.id) : false;

  return (
    <div className="flex flex-col gap-4">
      <Card className="flex flex-col gap-4">
        {isEditing ? (
          <EditPropertyInfoForm
            property={property}
            onSuccess={() => setIsEditing(false)}
            onCancel={() => setIsEditing(false)}
          />
        ) : (
          <>
            <div className="flex items-center justify-between">
              <h2 className="text-base font-semibold text-gray-900">Informations générales</h2>
              <Button type="button" variant="secondary" onClick={() => setIsEditing(true)}>
                Modifier
              </Button>
            </div>
            <dl className="flex flex-col gap-2 text-sm">
              <div>
                <dt className="text-gray-500">Nom</dt>
                <dd className="text-gray-900">{property.name}</dd>
              </div>
              <div>
                <dt className="text-gray-500">Adresse</dt>
                <dd className="text-gray-900">{property.address}</dd>
              </div>
            </dl>
          </>
        )}
      </Card>

      {canInviteMembers && (
        <Card className="flex flex-col gap-3">
          <h2 className="text-base font-semibold text-gray-900">Équipe</h2>
          <p className="text-sm text-gray-500">
            Invitez un membre (compte déjà existant) à rejoindre la gestion de cette copropriété.
          </p>
          <AddPropertyManagerForm propertyId={property.id} />
        </Card>
      )}
    </div>
  );
}
