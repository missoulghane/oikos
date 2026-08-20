import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { Card } from '@/shared/components/Card/Card';
import { Checkbox } from '@/shared/components/Checkbox/Checkbox';
import { Button } from '@/shared/components/Button/Button';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { useUnitTypeDefinitions } from '@/features/property-mngt/properties/hooks/useUnitTypeDefinitions';
import { useAddUnitTypeDefinition } from '@/features/property-mngt/properties/hooks/useAddUnitTypeDefinition';
import { useRemoveUnitTypeDefinition } from '@/features/property-mngt/properties/hooks/useRemoveUnitTypeDefinition';
import { UNIT_TYPE_CHOICES } from '@/features/property-mngt/properties/constants/unitTypeChoices';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

const CHOICE_NAMES = new Set<string>(UNIT_TYPE_CHOICES.map((choice) => choice.name));

/**
 * Les types de lots gérés par la copropriété, cochés dans la même liste que
 * celle du wizard d'inscription - toutes deux lisent UNIT_TYPE_CHOICES, le seul
 * endroit où cette liste se paramètre.
 *
 * <p>Rien n'est écrit tant que « Enregistrer » n'est pas cliqué : cocher trois
 * types envoyait autant d'appels, dont le deuxième pouvait échouer en laissant
 * l'écran à mi-chemin, sans que personne ne sache où.
 *
 * <p>Les prix et le mode de calcul ont quitté cet onglet pour la configuration
 * des échéances, où ils sont utilisés : ils ne décrivent pas la copropriété mais
 * la façon dont on lui appelle ses charges.
 */
export function PropertyUnitTypesTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const propertyId = property.id;
  const unitTypes = useUnitTypeDefinitions(propertyId);
  const addType = useAddUnitTypeDefinition(propertyId);
  const removeType = useRemoveUnitTypeDefinition(propertyId);

  // `null` tant que rien n'a été coché ni décoché : l'écran affiche alors ce que
  // dit le serveur, et un rafraîchissement en arrière-plan ne peut pas écraser
  // une sélection en cours. Remis à null après un enregistrement réussi, où
  // c'est de nouveau la liste du serveur qui fait foi.
  const [draft, setDraft] = useState<string[] | null>(null);
  const [isSaving, setIsSaving] = useState(false);
  const [savedAt, setSavedAt] = useState<number | null>(null);

  const defined = unitTypes.data;

  if (unitTypes.isLoading || !defined) {
    return <Loader label="Chargement des types de lots…" />;
  }

  const definedNames = defined.map((unitType) => unitType.name);
  const selected = draft ?? definedNames;
  const definedByName = new Map(defined.map((unitType) => [unitType.name, unitType]));

  // Les types déjà enregistrés qui ne sont pas dans la liste paramétrée :
  // hérités d'une saisie libre antérieure. Ils restent affichés et décochables,
  // sans quoi ils deviendraient invisibles et indéboulonnables.
  const extraNames = defined.map((unitType) => unitType.name).filter((name) => !CHOICE_NAMES.has(name));
  const rows = [...UNIT_TYPE_CHOICES.map((choice) => choice.name), ...extraNames];

  const added = selected.filter((name) => !definedByName.has(name));
  const removed = defined.filter((unitType) => !selected.includes(unitType.name));
  const hasChanges = added.length > 0 || removed.length > 0;

  function toggle(name: string, checked: boolean) {
    setSavedAt(null);
    setDraft((previous) => {
      const base = previous ?? definedNames;
      return checked ? [...base, name] : base.filter((selectedName) => selectedName !== name);
    });
  }

  async function save() {
    setIsSaving(true);
    setSavedAt(null);
    try {
      // Les retraits d'abord : ils peuvent échouer (un type porté par des lots),
      // et mieux vaut s'arrêter avant d'avoir ajouté quoi que ce soit que de
      // laisser la copropriété avec les nouveaux types et les anciens.
      for (const unitType of removed) {
        await removeType.mutateAsync(unitType.id);
      }
      for (const name of added) {
        await addType.mutateAsync(name);
      }
      setDraft(null);
      setSavedAt(Date.now());
    } catch {
      // Les hooks portent l'erreur, affichée ci-dessous.
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <Card className="flex flex-col gap-4">
        <div className="flex flex-col gap-1">
          <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">Types de lots gérés</h2>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Cochez les types de lots que compte votre copropriété. Ils déterminent ce que vous pourrez créer dans la
            liste des lots, et ce que vous tarifez dans la configuration des échéances.
          </p>
        </div>

        {unitTypes.isError && <Alert message={getErrorMessage(unitTypes.error)} />}
        {/* Un type porté par des lots ne se retire pas : l'API refuse, et le
            message dit lequel plutôt que de laisser la case revenir seule. */}
        {removeType.error && <Alert message={getErrorMessage(removeType.error)} />}
        {addType.error && <Alert message={getErrorMessage(addType.error)} />}
        {savedAt !== null && <Alert variant="success" message="Types de lots enregistrés." />}

        <div className="flex flex-col gap-3">
          {rows.map((name) => (
            <Checkbox
              key={name}
              name={`unit-type-${name}`}
              label={name}
              checked={selected.includes(name)}
              disabled={isSaving}
              onChange={(event) => toggle(name, event.target.checked)}
            />
          ))}
        </div>

        <div className="flex items-center gap-3">
          <Button type="button" isLoading={isSaving} disabled={!hasChanges} onClick={save}>
            Enregistrer
          </Button>
          {hasChanges && !isSaving && (
            <span className="text-sm text-gray-500 dark:text-gray-400">Modifications non enregistrées.</span>
          )}
        </div>
      </Card>
    </div>
  );
}
