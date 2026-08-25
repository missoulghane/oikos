import { Link, useNavigate, useParams } from 'react-router-dom';
import { Card } from '@/shared/components/Card/Card';
import { AddBuildingForm } from '@/features/property-mngt/properties/components/AddBuildingForm';

/**
 * Ajouter un immeuble est un écran, pas un bloc qui pousse la liste des lots
 * vers le bas : la saisie ouvrait un formulaire à même l'onglet, sous les
 * immeubles déjà présents, là où tout le reste du produit (nouvelle
 * copropriété, nouvelle AG) ouvre une page dédiée. Le formulaire vit dans une
 * carte, comme partout ailleurs - voir CreatePropertyPage, dont c'est le
 * jumeau.
 */
export function AddBuildingPage() {
  const { propertyId } = useParams<{ propertyId: string }>();
  const navigate = useNavigate();
  const lotsPath = `/property-mngt/properties/${propertyId}/property/lots`;

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link to={lotsPath} className="text-sm text-gray-500 dark:text-gray-400 hover:underline">
          ← Retour aux lots
        </Link>
        <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Nouvel immeuble</h1>
      </div>
      {/* Pleine largeur : la carte est le contenu de la page, pas un encart posé
          dans un coin - rien d'autre ne partage l'écran avec elle. */}
      <Card>
        <AddBuildingForm
          propertyId={propertyId ?? ''}
          onSuccess={() => navigate(lotsPath)}
          onCancel={() => navigate(lotsPath)}
        />
      </Card>
    </div>
  );
}
