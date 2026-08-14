/**
 * Lower-cases and strips diacritics so "echeance" matches "Échéance" and
 * "residence" matches "Résidence" - French labels are full of accents that a
 * user typing quickly (or on a phone keyboard) will not reproduce.
 */
export function normalizeForSearch(value: string): string {
  return value
    .toLowerCase()
    .normalize('NFD')
    // The combining-diacritic block, escaped rather than written literally so
    // the range survives any re-encoding of this file.
    .replace(/[\u0300-\u036f]/g, '');
}

/** True when every whitespace-separated term of the query appears in the haystack. */
export function matchesSearch(haystack: string, query: string): boolean {
  const normalizedHaystack = normalizeForSearch(haystack);
  return normalizeForSearch(query)
    .split(/\s+/)
    .filter(Boolean)
    .every((term) => normalizedHaystack.includes(term));
}
