import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { Card } from '@/shared/components/Card/Card';
import { Button } from '@/shared/components/Button/Button';
import { EditPropertyInfoForm } from '@/features/property-mngt/properties/components/EditPropertyInfoForm';
import { BoardSection } from '@/features/property-mngt/board-members';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function PropertyGeneralInfoTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const [isEditing, setIsEditing] = useState(false);

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
              <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">Informations générales</h2>
              <Button type="button" variant="secondary" onClick={() => setIsEditing(true)}>
                Modifier
              </Button>
            </div>
            <dl className="flex flex-col gap-2 text-sm">
              <div>
                <dt className="text-gray-500 dark:text-gray-400">Nom</dt>
                <dd className="text-gray-900 dark:text-white/90">{property.name}</dd>
              </div>
              <div>
                <dt className="text-gray-500 dark:text-gray-400">Adresse</dt>
                <dd className="text-gray-900 dark:text-white/90">{property.address}</dd>
              </div>
              <div>
                <dt className="text-gray-500 dark:text-gray-400">Ville</dt>
                {/* Un tiret plutôt qu'une ligne absente : les copropriétés créées
                    avant l'existence du champ n'ont pas de ville, et la faire
                    disparaître laisserait croire qu'elle n'est pas demandée. */}
                <dd className="text-gray-900 dark:text-white/90">{property.city || '—'}</dd>
              </div>
            </dl>
          </>
        )}
      </Card>

      {/* Le conseil syndical occupait un onglet à lui seul : deux écrans pour
          lire qui gère la copropriété et laquelle. */}
      <BoardSection propertyId={property.id} />
    </div>
  );
}
