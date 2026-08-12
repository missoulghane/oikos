import type { Paged } from '@/shared/types/pagination.types';

export type ConversationType = 'GROUP' | 'BOARD_PRIVATE' | 'BROADCAST';

/** Which hat a message was sent under - "en tant que copropriétaire" vs "en tant que [rôle] du
 * bureau". Always BOARD for BOARD_PRIVATE/BROADCAST; a real choice only for GROUP, and only when
 * the sender holds both roles on the property. */
export type SenderIdentity = 'OWNER' | 'BOARD';

export interface ConversationParticipant {
  userId: string;
  fullName: string;
}

export interface ConversationSummary {
  id: string;
  type: ConversationType;
  propertyId: string;
  propertyName: string;
  subject: string | null; // GROUP conversation title, chosen at compose time; always null for BROADCAST
  /** Optional lot label the GROUP thread concerns (e.g. "Appartement 3"), chosen at compose time -
   * disambiguates a recipient who owns several units in the property. Free text, never a foreign
   * key to the unit registry - a display hint, not a structural link. Always null otherwise. */
  concernsUnit: string | null;
  participants: ConversationParticipant[]; // every OTHER participant (excludes the caller); empty for BROADCAST
  lastMessagePreview: string | null;
  lastMessageAt: string | null; // ISO instant
  unreadCount: number;
  // Every conversation has at least one message (composing always sends the
  // first one immediately). A count of 1 is just a message; >1 is an actual
  // conversation (a message with replies) - see ConversationListItem, which
  // only shows a "N messages" indicator once this is >1.
  messageCount: number;
}

export type PagedConversations = Paged<ConversationSummary>;

export interface UnreadSummary {
  unreadConversationCount: number;
  totalUnreadMessageCount: number;
  recentUnread: ConversationSummary[]; // up to 5
}

export interface Message {
  id: string;
  conversationId: string;
  senderId: string;
  senderName: string;
  senderIdentity: SenderIdentity;
  body: string;
  createdAt: string; // ISO instant
  mine: boolean;
}

export type PagedMessages = Paged<Message>;

export interface RecipientCandidate {
  userId: string;
  fullName: string;
  roleLabel: string;
  /** Every unit this candidate owns in the property - empty for a board/manager seat that owns nothing there. */
  unitNumbers: string[];
  /** True for a board/manager seat - drives the "Le bureau" pseudo-recipient and the recipient row's chip. */
  isStaff: boolean;
}

export interface StartConversationPayload {
  recipientUserIds: string[];
  subject: string;
  body: string;
  /** Omitted when the sender is only eligible for one identity on the property - required only when they hold both. */
  senderIdentity?: SenderIdentity;
  /** Optional lot label this thread concerns (see ConversationSummary.concernsUnit) - only offered
   * in the compose UI when the single selected recipient owns more than one unit here. */
  concernsUnit?: string;
}

export interface StartConversationResult {
  conversationId: string;
}

export interface StartBoardConversationPayload {
  subject: string;
  body: string;
}

export interface SendMessagePayload {
  body: string;
  senderIdentity?: SenderIdentity;
}

export interface SendBroadcastMessagePayload {
  body: string;
}

/** Réception = the caller has received at least one message in the conversation (someone else
 * posted); Envoyé = the caller has sent at least one message in it. A back-and-forth conversation
 * matches both. */
export type ConversationBox = 'RECEIVED' | 'SENT';

export interface MessageDraftSummary {
  id: string;
  propertyId: string;
  propertyName: string;
  broadcast: boolean;
  recipients: ConversationParticipant[]; // resolved display names; always empty when broadcast is true
  subject: string | null;
  body: string | null;
  lastModifiedAt: string; // ISO instant
}

export type PagedMessageDrafts = Paged<MessageDraftSummary>;

export interface MessageDraft {
  id: string;
  propertyId: string;
  broadcast: boolean;
  recipients: ConversationParticipant[];
  subject: string | null;
  body: string | null;
}

export interface SaveMessageDraftPayload {
  recipientUserIds: string[];
  broadcast: boolean;
  subject: string | null;
  body: string | null;
}

export interface MessageDraftReference {
  draftId: string;
}
