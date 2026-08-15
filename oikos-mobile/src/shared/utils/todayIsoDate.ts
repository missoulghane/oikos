/**
 * Today as "YYYY-MM-DD", from the local calendar fields rather than
 * toISOString(), which converts to UTC first and returns yesterday's date for
 * any timezone east of Greenwich during the early hours. Mirrors oikos-web's.
 */
export function todayIsoDate(today: Date = new Date()): string {
  const month = `${today.getMonth() + 1}`.padStart(2, '0');
  const day = `${today.getDate()}`.padStart(2, '0');
  return `${today.getFullYear()}-${month}-${day}`;
}
