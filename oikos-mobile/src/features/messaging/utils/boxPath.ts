import type { ConversationBox } from '@/features/messaging/types/messaging.types';

export const BOX_LABEL: Record<ConversationBox, string> = {
  RECEIVED: 'Réception',
  SENT: 'Envoyé',
};

export const BOX_EMPTY_STATE: Record<ConversationBox, string> = {
  RECEIVED: 'Aucun message reçu',
  SENT: 'Aucun message envoyé',
};
