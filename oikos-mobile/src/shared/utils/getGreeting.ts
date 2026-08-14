// 5h-17h59 -> Bonjour, 18h-4h59 -> Bonsoir.
export function getGreeting(date: Date = new Date()): 'Bonjour' | 'Bonsoir' {
  const hour = date.getHours();
  return hour >= 5 && hour < 18 ? 'Bonjour' : 'Bonsoir';
}
