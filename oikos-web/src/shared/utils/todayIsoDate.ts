/**
 * Today as "YYYY-MM-DD", built from the local calendar fields rather than
 * toISOString(), which converts to UTC first and so returns yesterday's date for
 * any timezone east of Greenwich during the early hours. Comparisons against the
 * API's own ISO date strings then stay lexicographic, with no Date parsing at all.
 */
export function todayIsoDate(today: Date = new Date()): string {
  const month = `${today.getMonth() + 1}`.padStart(2, '0');
  const day = `${today.getDate()}`.padStart(2, '0');
  return `${today.getFullYear()}-${month}-${day}`;
}
