import { SUPPORT_EMAIL } from '@/config/env';

/**
 * Identité de l'éditeur reprise dans les CGU et les CGV. Un seul endroit à
 * compléter : les deux documents lisent tout d'ici, et une information citée
 * dans l'un et oubliée dans l'autre est exactement ce qu'un contrôle relève.
 *
 * Les valeurs entre crochets sont à renseigner avant toute mise en production -
 * elles s'affichent telles quelles, à dessein : un champ vide passerait
 * inaperçu, un crochet ne passe pas.
 */
export const LEGAL_ENTITY = {
  productName: 'Daba Syndic',
  website: 'daba-syndic.com',
  companyName: '[Raison sociale]',
  legalForm: '[Forme juridique et capital social]',
  headOffice: '[Adresse du siège social]',
  tradeRegister: '[N° RC et tribunal d’immatriculation]',
  ice: '[N° ICE]',
  taxId: '[N° IF]',
  publicationDirector: '[Nom du responsable de la publication]',
  host: '[Hébergeur : nom, adresse et pays d’hébergement des données]',
  cndpDeclaration: '[N° de déclaration ou d’autorisation CNDP]',
  competentCourt: '[Ville du tribunal compétent]',
  contactEmail: SUPPORT_EMAIL,
} as const;

/** Version affichée en tête des deux documents ; à changer à chaque modification de leur contenu. */
export const LEGAL_DOCUMENTS_VERSION = { label: '1.0', date: '28 octobre 2026' } as const;
