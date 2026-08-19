import { z } from 'zod';

/**
 * Numéro au format international, tel que le compose PhoneField : '+', indicatif
 * pays puis le numéro national, sans séparateur. Le maximum de 20 caractères est
 * celui de l'API (RegisterUserRequest et consorts, colonne phone) ; le maximum de
 * 15 chiffres est celui de la norme E.164, qu'aucun opérateur ne dépasse.
 */
export const phoneSchema = z
  .string()
  .trim()
  .min(1, 'Le numéro de téléphone est requis')
  .max(20, '20 caractères maximum')
  .regex(/^\+[1-9]\d{7,14}$/, 'Numéro invalide : saisissez-le sans le 0 initial');
