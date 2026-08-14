/**
 * How many filter fields differ from their default value - drives the count
 * badge on the "Filtres" toggle and whether "Effacer" is offered at all.
 * Sort fields are normally passed in `ignore`: they always hold a value, so
 * counting them would make the badge read "2 filtres actifs" on a pristine list.
 */
export function countActiveFilters<T extends object>(value: T, defaults: T, ignore: readonly (keyof T)[] = []): number {
  return (Object.keys(defaults) as (keyof T)[])
    .filter((key) => !ignore.includes(key))
    .filter((key) => value[key] !== defaults[key]).length;
}
