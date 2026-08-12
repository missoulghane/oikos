import { useLocation } from 'react-router-dom';

/**
 * The space the viewer is currently in, derived entirely from the URL
 * rather than a client store: 'owner' is the consolidated copropriétaire
 * space (never scoped to one property), 'board' is scoped to one property
 * at a time, 'neutral' means the URL carries no space at all. Property-mngt
 * and property-ownership routes carry it in the path itself; every other
 * route (dashboard, messages, profile…) carries it via ?space=owner or
 * ?space=board&propertyId=<id> instead, since those are shared across every
 * space rather than scoped to one - a link into Messagerie built from the
 * board space must say so explicitly, or there is nothing in the URL to
 * resolve it from and useEffectiveSpace's landing default (owner-first)
 * silently overrides whatever space the viewer was just in.
 *
 * Kept URL-driven so a link/bookmark/notification reproduces the exact same
 * space on reload, and so the same addressing scheme is reusable as-is by
 * deep links on a future oikos-mobile. Naming is English throughout (kind
 * values, query params) - only the labels shown to the user are French,
 * same as the rest of the app.
 */
export type ActiveSpace =
  | { kind: 'owner' }
  | { kind: 'board'; propertyId: string | null }
  | { kind: 'neutral' };

const PROPERTY_MNGT_ID_RE = /^\/property-mngt\/properties\/([^/]+)/;

export function useActiveSpace(): ActiveSpace {
  const { pathname, search } = useLocation();

  if (pathname.startsWith('/property-ownership')) {
    return { kind: 'owner' };
  }
  if (pathname.startsWith('/property-mngt')) {
    return { kind: 'board', propertyId: pathname.match(PROPERTY_MNGT_ID_RE)?.[1] ?? null };
  }

  const params = new URLSearchParams(search);
  const space = params.get('space');
  if (space === 'owner') {
    return { kind: 'owner' };
  }
  if (space === 'board') {
    const propertyId = params.get('propertyId');
    return propertyId ? { kind: 'board', propertyId } : { kind: 'neutral' };
  }
  return { kind: 'neutral' };
}
