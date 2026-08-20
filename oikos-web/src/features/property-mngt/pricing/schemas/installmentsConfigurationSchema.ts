import { z } from 'zod';
import { DUES_CALCULATION_MODES } from '@/features/property-mngt/properties/types/property.types';

/**
 * Le mode de gestion et ce qu'il commande, dans un seul schéma : un forfait sans
 * prix ou des tantièmes sans budget sont des états incohérents, et les valider
 * séparément laissait passer l'un comme l'autre.
 *
 * Mirrors UpdateDuesCalculationModeRequest, SetProjectedBudgetRequest et
 * SetUnitTypePriceRequest (oikos-api).
 */
export const installmentsConfigurationSchema = z
  .object({
    mode: z.enum(DUES_CALCULATION_MODES, { error: 'Le mode de gestion est requis' }),
    projectedBudget: z.number({ error: 'Le budget prévisionnel est requis' }).min(0, 'Ne peut pas être négatif').optional(),
    prices: z.array(z.object({ price: z.number({ error: 'Le prix est requis' }).min(0, 'Ne peut pas être négatif') })),
  })
  .refine((values) => values.mode !== 'SHARES' || values.projectedBudget !== undefined, {
    message: 'Le budget prévisionnel est requis en mode tantièmes',
    path: ['projectedBudget'],
  });

export type InstallmentsConfigurationFormValues = z.infer<typeof installmentsConfigurationSchema>;
