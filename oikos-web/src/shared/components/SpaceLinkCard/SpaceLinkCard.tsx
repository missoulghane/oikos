import { Link } from 'react-router-dom';

interface SpaceLinkCardProps {
  to: string;
  /** Which space this card leads into - drives color only, never shape: the two must stay visually paired. */
  tone: 'owner' | 'board';
  title: string;
  subtitle: string;
}

const TONE_CLASSES: Record<SpaceLinkCardProps['tone'], { card: string; title: string; chevron: string }> = {
  owner: {
    card: 'border-success-200 bg-success-25 hover:bg-success-50 dark:border-success-500/25 dark:bg-success-500/10 dark:hover:bg-success-500/15',
    title: 'text-success-700 dark:text-success-400',
    chevron: 'text-success-500 dark:text-success-400',
  },
  board: {
    card: 'border-warning-200 bg-warning-25 hover:bg-warning-50 dark:border-warning-500/25 dark:bg-warning-500/10 dark:hover:bg-warning-500/15',
    title: 'text-warning-700 dark:text-warning-400',
    chevron: 'text-warning-500 dark:text-warning-400',
  },
};

/**
 * The card that lets a copropriétaire+bureau account move to the other
 * space from the dashboard (see MandateCard and BoardDashboard's "Revenir à
 * l'espace copropriétaire") - a mention, never an automatic switch. Same
 * shape for both directions, colored by destination (vert copropriétaire /
 * ambre bureau) so the pair reads as one consistent affordance rather than
 * two unrelated pieces of UI.
 */
export function SpaceLinkCard({ to, tone, title, subtitle }: SpaceLinkCardProps) {
  const classes = TONE_CLASSES[tone];
  return (
    <Link
      to={to}
      className={`flex items-center justify-between rounded-2xl border p-4 transition-colors ${classes.card}`}
    >
      <div>
        <p className={`text-sm font-semibold ${classes.title}`}>{title}</p>
        <p className="text-sm text-gray-500 dark:text-gray-400">{subtitle}</p>
      </div>
      <span className={classes.chevron}>›</span>
    </Link>
  );
}
