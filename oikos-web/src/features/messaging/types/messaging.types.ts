import type { Paged } from '@/shared/types/pagination.types';

export type ConversationType = 'GROUP' | 'BROADCAST';

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
  body: string;
  createdAt: string; // ISO instant
  mine: boolean;
}

export type PagedMessages = Paged<Message>;

export interface RecipientCandidate {
  userId: string;
  fullName: string;
  roleLabel: string;
}

export interface StartConversationPayload {
  recipientUserIds: string[];
  subject: string;
  body: string;
}

export interface StartConversationResult {
  conversationId: string;
}

export interface SendMessagePayload {
  body: string;
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
