import { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { useForm, useWatch } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useUnitTypeDefinitions } from '@/features/property-mngt/properties/hooks/useUnitTypeDefinitions';
import { useUnitTypePrices } from '@/features/property-mngt/pricing/hooks/useUnitTypePrices';
import { useSetUnitTypePrice } from '@/features/property-mngt/pricing/hooks/useSetUnitTypePrice';
import { useSetProjectedBudget } from '@/features/property-mngt/pricing/hooks/useSetProjectedBudget';
import { useUpdateDuesCalculationMode } from '@/features/property-mngt/pricing/hooks/useUpdateDuesCalculationMode';
import { DUES_CALCULATION_MODE_LABELS } from '@/features/property-mngt/properties/constants/duesCalculationModeLabels';
import {
  installmentsConfigurationSchema,
  type InstallmentsConfigurationFormValues,
} from '@/features/property-mngt/pricing/schemas/installmentsConfigurationSchema';
import { Card } from '@/shared/components/Card/Card';
import { Input } from '@/shared/components/Input/Input';
import { RadioGroup } from '@/shared/components/RadioGroup/RadioGroup';
import { Button } from '@/shared/components/Button/Button';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

const MODE_OPTIONS = Object.entries(DUES_CALCULATION_MODE_LABELS).map(([value, label]) => ({ value, label }));

/** Un type de lot sans prix vaut 0, pas « rien » : un appel de fonds doit pouvoir se calculer dès le premier jour. */
const DEFAULT_PRICE = 0;

/**
 * Comment la copropriété appelle ses charges : au forfait par type de lot, ou au
 * prorata des tantièmes sur un budget prévisionnel.
 *
 * <p>Un seul formulaire et un seul bouton : le mode et les prix qu'il commande
 * se lisent ensemble, et les enregistrer séparément laissait passer l'état
 * intermédiaire - un forfait déclaré dont les prix n'étaient pas encore saisis.
 * Le bloc affiché suit la valeur du formulaire, pas celle enregistrée : basculer
 * de tantièmes à forfait montre les prix immédiatement, avant tout aller-retour
 * serveur.
 *
 * <p>Cet écran vivait dans l'onglet Configuration de la copropriété, à côté de
 * choses qui la décrivent (son nom, ses lots). Il a rejoint la gestion des
 * échéances, seul endroit où ces réglages servent : on n'y vient pas pour
 * décrire la copropriété mais pour préparer un appel de fonds.
 */
export function InstallmentsConfigurationTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const propertyId = property.id;
  const unitTypes = useUnitTypeDefinitions(propertyId);
  const unitTypePrices = useUnitTypePrices(propertyId);

  const updateMode = useUpdateDuesCalculationMode(propertyId);
  const setProjectedBudget = useSetProjectedBudget(propertyId);
  const setUnitTypePrice = useSetUnitTypePrice(propertyId);

  const [isSaving, setIsSaving] = useState(false);
  const [isSaved, setIsSaved] = useState(false);

  const {
    register,
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<InstallmentsConfigurationFormValues>({
    resolver: zodResolver(installmentsConfigurationSchema),
    defaultValues: { mode: property.duesCalculationMode, projectedBudget: property.projectedBudget ?? DEFAULT_PRICE, prices: [] },
  });
  const mode = useWatch({ control, name: 'mode' });

  const definitions = unitTypes.data;
  const prices = unitTypePrices.data;

  // Le formulaire se recharge dès que le serveur a répondu : avant, les lignes
  // de prix n'existent pas encore, et un defaultValues vide les figerait.
  useEffect(() => {
    if (!definitions || !prices) {
      return;
    }
    const priceByUnitTypeId = new Map(prices.map((entry) => [entry.unitTypeId, entry.price]));
    reset({
      mode: property.duesCalculationMode,
      projectedBudget: property.projectedBudget ?? DEFAULT_PRICE,
      // Indexées comme `definitions` : c'est cet ordre qui relie une ligne du
      // formulaire à son type au moment d'enregistrer.
      prices: definitions.map((definition) => ({ price: priceByUnitTypeId.get(definition.id) ?? DEFAULT_PRICE })),
    });
  }, [definitions, prices, property.duesCalculationMode, property.projectedBudget, reset]);

  if (unitTypes.isLoading || unitTypePrices.isLoading) {
    return <Loader label="Chargement de la configuration…" />;
  }

  const persistedPriceByUnitTypeId = new Map((prices ?? []).map((entry) => [entry.unitTypeId, entry.price]));
  const saveError = updateMode.error ?? setProjectedBudget.error ?? setUnitTypePrice.error;

  async function onSubmit(values: InstallmentsConfigurationFormValues) {
    setIsSaving(true);
    setIsSaved(false);
    try {
      if (values.mode !== property.duesCalculationMode) {
        await updateMode.mutateAsync(values.mode);
      }
      if (values.mode === 'SHARES') {
        if (values.projectedBudget !== property.projectedBudget) {
          await setProjectedBudget.mutateAsync(values.projectedBudget ?? DEFAULT_PRICE);
        }
      } else {
        // Seuls les prix qui bougent partent - y compris ceux d'un type qui n'en
        // avait aucun, que le formulaire a initialisés à 0.
        for (const [index, definition] of (definitions ?? []).entries()) {
          const price = values.prices[index]?.price ?? DEFAULT_PRICE;
          if (persistedPriceByUnitTypeId.get(definition.id) !== price) {
            await setUnitTypePrice.mutateAsync({ unitTypeId: definition.id, price });
          }
        }
      }
      setIsSaved(true);
    } catch {
      // Les hooks portent l'erreur, affichée ci-dessous.
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      <Card className="flex flex-col gap-5">
        <div className="flex flex-col gap-1">
          <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">Calcul des appels de fonds</h2>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Le mode de gestion détermine ce qui est demandé à chaque copropriétaire : un forfait par type de lot, ou
            une part du budget prévisionnel au prorata des tantièmes.
          </p>
        </div>

        {unitTypes.isError && <Alert message={getErrorMessage(unitTypes.error)} />}
        {unitTypePrices.isError && <Alert message={getErrorMessage(unitTypePrices.error)} />}
        {saveError && <Alert message={getErrorMessage(saveError)} />}
        {isSaved && <Alert variant="success" message="Configuration enregistrée." />}

        <RadioGroup
          label="Mode de gestion"
          options={MODE_OPTIONS}
          {...register('mode')}
          errorMessage={errors.mode?.message}
        />

        {mode === 'SHARES' ? (
          <Input
            label="Budget prévisionnel annuel"
            type="number"
            min={0}
            step="any"
            {...register('projectedBudget', { valueAsNumber: true })}
            errorMessage={errors.projectedBudget?.message}
          />
        ) : (
          <div className="flex flex-col gap-3">
            <h3 className="text-sm font-medium text-gray-900 dark:text-white/90">Prix par type de lot</h3>
            {definitions && definitions.length === 0 ? (
              <EmptyState title="Aucun type de lot pour le moment">
                Cochez les types de lots de votre copropriété dans l'onglet Configuration de « Ma copropriété » pour
                pouvoir leur associer un prix.
              </EmptyState>
            ) : (
              <div className="grid gap-4 rounded-lg border border-gray-200 p-4 dark:border-gray-800 sm:grid-cols-2">
                {/* Le nom du type sert de libellé : l'intitulé « Prix » est déjà
                    porté par le titre du bloc, le répéter par ligne l'alourdit. */}
                {(definitions ?? []).map((definition, index) => (
                  <Input
                    key={definition.id}
                    label={definition.name}
                    type="number"
                    min={0}
                    step="any"
                    {...register(`prices.${index}.price`, { valueAsNumber: true })}
                    errorMessage={errors.prices?.[index]?.price?.message}
                  />
                ))}
              </div>
            )}
          </div>
        )}

        <div>
          <Button type="submit" isLoading={isSaving}>
            Enregistrer
          </Button>
        </div>
      </Card>
    </form>
  );
}
