import type { ConversationBox } from '@/features/messaging/types/messaging.types';

/** Base list path for each mailbox tab - shared by MessagingLayout (its own "Nouveau message"/
 * pagination links), ConversationListItem (row -> detail link) and ConversationPage (mobile
 * back link), which must all point at whichever box (Réception/Envoyé) is currently open rather
 * than a single hardcoded route. */
export const BOX_PATH: Record<ConversationBox, string> = {
  RECEIVED: '/messages/reception',
  SENT: '/messages/sent',
};
