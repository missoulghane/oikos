/** Mirrors BoardRoleResponse (oikos-api): a seat on the board, from the board_role table. */
export interface BoardRoleOption {
  code: string;
  label: string;
  position: number;
  /** Proposé au syndic bénévole à l'inscription (étape 1 du wizard). */
  availableAtSignup: boolean;
}

/** Mirrors UnitTypeChoiceResponse (oikos-api): a unit type on offer, from the unit_type_choice table. */
export interface UnitTypeChoice {
  name: string;
  /** Pour les récapitulatifs (« 12 appartements · 8 box »). */
  plural: string;
  description: string;
  position: number;
}
