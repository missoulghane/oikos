import { z } from 'zod';

/**
 * Mirrors the bean validation constraints on AddUnitRequest (oikos-api), plus
 * la borne haute de l'étage, qui n'existe que côté immeuble : elle dépend du
 * floorCount du bâtiment visé, d'où un schéma construit par bâtiment plutôt
 * qu'une constante. L'API revérifie la même règle (AddUnitService) - ici c'est
 * pour le dire avant l'aller-retour, pas à la place.
 */
export function addUnitSchema(buildingFloorCount: number) {
  return z.object({
    unitTypeId: z.string().min(1, 'Le type de lot est requis'),
    // Chiffres seuls : le « N° » est posé par l'interface (voir unitNumberFromDigits),
    // et un numéro qui mélangerait lettres et chiffres casserait le tri de la liste
    // des lots autant que la nomenclature générée à la création de la copropriété.
    unitNumber: z
      .string()
      .trim()
      .min(1, 'Le numéro de lot est requis')
      .max(10, '10 chiffres maximum')
      .regex(/^\d+$/, 'Le numéro de lot ne peut contenir que des chiffres'),
    // Facultatif : « étage inconnu » est un état légitime, et le rez-de-chaussée est 0.
    floor: z
      .number()
      .int('Doit être un nombre entier')
      .min(0, 'Ne peut pas être négatif')
      .max(
        buildingFloorCount,
        buildingFloorCount === 0
          ? "Cet immeuble n'a que le rez-de-chaussée"
          : `Cet immeuble compte ${buildingFloorCount} étage(s) au-dessus du rez-de-chaussée`,
      )
      .nullable(),
    shares: z.number({ error: 'Les tantièmes sont requis' }).min(0, 'Ne peut pas être négatif'),
  });
}

export type AddUnitFormValues = z.infer<ReturnType<typeof addUnitSchema>>;
