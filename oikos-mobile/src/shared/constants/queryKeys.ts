// Mirrors the shape of oikos-web's shared/constants/queryKeys.ts, ported
// incrementally as each domain lands on mobile - only the `me` keys exist so
// far (identity phase). Add sibling top-level keys (properties, messaging,
// notifications…) as their features are ported, following the same pattern.
export const queryKeys = {
  me: {
    detail: () => ['me', 'detail'] as const,
    avatar: (hasAvatar: boolean) => ['me', 'avatar', hasAvatar] as const,
    units: () => ['me', 'units'] as const,
    installments: () => ['me', 'installments'] as const,
    payments: () => ['me', 'payments'] as const,
    membershipRequests: () => ['me', 'membership-requests'] as const,
  },
  invitations: {
    preview: (token: string) => ['invitations', 'preview', token] as const,
    availableUnits: (token: string) => ['invitations', token, 'available-units'] as const,
  },
  units: {
    owners: (unitId: string) => ['units', unitId, 'owners'] as const,
  },
  installments: {
    detail: (installmentId: string) => ['installments', 'detail', installmentId] as const,
  },
  notifications: {
    list: (page: number, size: number) => ['notifications', 'list', page, size] as const,
    unreadCount: () => ['notifications', 'unread-count'] as const,
  },
  messaging: {
    conversations: (page: number, size: number, search: string | undefined, box: 'RECEIVED' | 'SENT') =>
      ['messaging', 'conversations', page, size, search, box] as const,
    unreadSummary: () => ['messaging', 'unread-summary'] as const,
    messages: (conversationId: string) => ['messaging', 'conversations', conversationId, 'messages'] as const,
    recipients: (propertyId: string, search: string | undefined) =>
      ['messaging', 'recipients', propertyId, search] as const,
    drafts: (page: number, size: number, search: string | undefined) =>
      ['messaging', 'drafts', page, size, search] as const,
    draft: (draftId: string) => ['messaging', 'drafts', draftId] as const,
  },
};
