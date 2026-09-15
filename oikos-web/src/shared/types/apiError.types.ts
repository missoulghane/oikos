export interface ApiErrorBody {
  status: number;
  error: string;
  message: string;
  /**
   * Renseigné uniquement pour les erreurs que l'interface doit distinguer entre
   * elles (voir ErrorCodes.java côté API) ; `null` partout ailleurs. C'est le seul
   * champ de ce corps qui pilote un affichage : `message` est technique et ne sort
   * jamais de la console réseau.
   */
  code?: string | null;
  path: string;
  timestamp: string;
}
