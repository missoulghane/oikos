/**
 * Dit une fois, pour tout un formulaire, ce que l'astérisque des étiquettes
 * signifie - au lieu d'accoler « (facultatif) » à chaque champ qui ne l'est
 * pas. Marquer l'obligatoire plutôt que le facultatif tient parce que les
 * champs requis sont la majorité : une ligne discrète remplace autant de
 * parenthèses qu'il y a d'exceptions, et l'œil ne relit plus le même mot à
 * chaque ligne du formulaire.
 */
export function RequiredFieldsHint() {
  return <p className="text-sm text-gray-500 dark:text-gray-400">Les champs suivis d'un * sont obligatoires.</p>;
}
