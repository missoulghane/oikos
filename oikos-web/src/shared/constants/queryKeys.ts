export const queryKeys = {
  me: {
    detail: () => ['me', 'detail'] as const,
    units: () => ['me', 'units'] as const,
    installments: () => ['me', 'installments'] as const,
    payments: () => ['me', 'payments'] as const,
    membershipRequests: () => ['me', 'membership-requests'] as const,
    avatar: (hasAvatar: boolean) => ['me', 'avatar', hasAvatar] as const,
  },
  properties: {
    list: (page: number, size: number) => ['properties', 'list', page, size] as const,
    detail: (id: string) => ['properties', 'detail', id] as const,
    buildings: (propertyId: string, page: number, size: number) =>
      ['properties', propertyId, 'buildings', page, size] as const,
    /**
     * Takes the filter object whole rather than one parameter per criterion.
     * React Query hashes it deterministically, and a new filter can no longer
     * be added to the query without reaching the key: leaving one out served
     * stale rows until something else - a page change - moved the key.
     */
    installments: (propertyId: string, page: number, size: number, filters: object) =>
      ['properties', propertyId, 'installments', page, size, filters] as const,
    /** The lot count of a whole copropriété, which no other endpoint carries. */
    unitCount: (propertyId: string) => ['properties', propertyId, 'unit-count'] as const,
    /** What is unpaid and already due - the syndic dashboard's "à collecter". */
    installmentCollectionSummary: (propertyId: string) =>
      ['properties', propertyId, 'installments', 'collection-summary'] as const,
    unitTypePrices: (propertyId: string) => ['properties', propertyId, 'unit-type-prices'] as const,
    unitTypes: (propertyId: string) => ['properties', propertyId, 'unit-types'] as const,
    installmentCalls: (propertyId: string, page: number, size: number) =>
      ['properties', propertyId, 'installment-calls', page, size] as const,
    /** Filters passed whole, for the reason given on `installments` below. */
    contacts: (propertyId: string, page: number, size: number, filters: object) =>
      ['properties', propertyId, 'contacts', page, size, filters] as const,
    accountingOpenExercise: (propertyId: string) =>
      ['properties', propertyId, 'accounting', 'open-exercise'] as const,
    accountingLedgerAccounts: (propertyId: string) =>
      ['properties', propertyId, 'accounting', 'ledger-accounts'] as const,
    accountingJournalEntries: (propertyId: string, page: number, size: number) =>
      ['properties', propertyId, 'accounting', 'entries', page, size] as const,
    accountingJournalEntry: (propertyId: string, entryId: string) =>
      ['properties', propertyId, 'accounting', 'entries', entryId] as const,
    accountingExpenses: (propertyId: string) => ['properties', propertyId, 'accounting', 'expenses'] as const,
    latestPayments: (propertyId: string, size: number) =>
      ['properties', propertyId, 'payments', 'latest', size] as const,
    /** Filters passed whole, for the reason given on `installments` above. */
    ledgerAccountEntries: (
      propertyId: string,
      accountId: string,
      page: number,
      size: number,
      filters: object,
    ) =>
      [
        'properties',
        propertyId,
        'accounting',
        'ledger-accounts',
        accountId,
        'entries',
        page,
        size,
        filters,
      ] as const,
  },
  installmentCalls: {
    detail: (id: string) => ['installment-calls', id, 'detail'] as const,
  },
  buildings: {
    /** Filters passed whole, for the reason given on `properties.installments`. */
    units: (buildingId: string, page: number, size: number, filters: object) =>
      ['buildings', buildingId, 'units', page, size, filters] as const,
  },
  units: {
    detail: (unitId: string) => ['units', unitId, 'detail'] as const,
    owners: (unitId: string) => ['units', unitId, 'owners'] as const,
    installments: (unitId: string) => ['units', unitId, 'installments'] as const,
    payments: (unitId: string) => ['units', unitId, 'payments'] as const,
  },
  installments: {
    detail: (installmentId: string) => ['installments', 'detail', installmentId] as const,
  },
  parties: {
    /** Filters passed whole, for the reason given on `properties.installments`. */
    list: (propertyId: string | undefined, page: number, size: number, filters: object) =>
      ['parties', 'list', propertyId, page, size, filters] as const,
    detail: (id: string) => ['parties', 'detail', id] as const,
    lots: (id: string) => ['parties', id, 'lots'] as const,
    /** « Qui se connecte avec cette adresse ? » - interrogé au fil de la frappe, d'où la clé par email. */
    byAccountEmail: (propertyId: string, email: string) =>
      ['parties', propertyId, 'by-account-email', email] as const,
  },
  invitations: {
    preview: (token: string) => ['invitations', 'preview', token] as const,
    availableUnits: (token: string) => ['invitations', token, 'available-units'] as const,
    list: (propertyId: string, page: number, size: number) =>
      ['invitations', propertyId, 'list', page, size] as const,
    detail: (id: string) => ['invitations', 'detail', id] as const,
    /** Le lien public de la copropriété : unique et permanent, donc pas de page ni de filtre. */
    publicLink: (propertyId: string) => ['invitations', propertyId, 'public-link'] as const,
    membershipRequests: (propertyId: string, page: number, size: number, filters: object) =>
      ['invitations', propertyId, 'membership-requests', page, size, filters] as const,
  },
  boardMembers: {
    list: (propertyId: string) => ['board-members', propertyId, 'list'] as const,
  },
  documents: {
    list: (ownerType: string, ownerId: string, page: number, size: number) =>
      ['documents', ownerType, ownerId, 'list', page, size] as const,
  },
  messaging: {
    conversations: (
      page: number,
      size: number,
      search: string | undefined,
      box: 'RECEIVED' | 'SENT',
      readState?: 'READ' | 'UNREAD',
    ) => ['messaging', 'conversations', page, size, search, box, readState] as const,
    unreadSummary: () => ['messaging', 'unread-summary'] as const,
    recipientGroups: (propertyId: string) => ['messaging', 'recipient-groups', propertyId] as const,
    messages: (conversationId: string) => ['messaging', 'conversations', conversationId, 'messages'] as const,
    recipients: (propertyId: string, search: string | undefined) =>
      ['messaging', 'recipients', propertyId, search] as const,
    drafts: (page: number, size: number, search: string | undefined) =>
      ['messaging', 'drafts', page, size, search] as const,
    draft: (draftId: string) => ['messaging', 'drafts', draftId] as const,
  },
  generalMeetings: {
    /** Filters passed whole, for the reason given on `properties.installments`. */
    list: (propertyId: string, page: number, size: number, filters: object) =>
      ['general-meetings', propertyId, 'list', page, size, filters] as const,
    detail: (meetingId: string) => ['general-meetings', meetingId, 'detail'] as const,
    agendaItems: (meetingId: string) => ['general-meetings', meetingId, 'agenda-items'] as const,
    convocations: (meetingId: string) => ['general-meetings', meetingId, 'convocations'] as const,
    /** Its own key: the single fetch carries the confirmation code, the list never does. */
    convocation: (convocationId: string) => ['convocations', convocationId] as const,
    /** Not scoped by meeting or property: the channel catalog is the product's, global. */
    convocationChannels: () => ['convocation-channels'] as const,
    replyMedia: () => ['reply-media'] as const,
    /** Keyed by the token itself: the anonymous page has no other identifier to key on. */
    convocationConfirmation: (token: string) => ['convocations', 'by-token', token] as const,
    convocationByCode: (meetingReference: string, code: string) =>
      ['convocations', 'by-reference', meetingReference, code] as const,
    attendanceSummary: (meetingId: string) => ['general-meetings', meetingId, 'attendance-summary'] as const,
    minutes: (meetingId: string) => ['general-meetings', meetingId, 'minutes'] as const,
    quorumSettings: (propertyId: string) => ['general-meetings', propertyId, 'quorum-settings'] as const,
    /** Keyed by agenda item, not by meeting: each ballot is read and refreshed on its own. */
    result: (agendaItemId: string) => ['agenda-items', agendaItemId, 'result'] as const,
    votes: (agendaItemId: string) => ['agenda-items', agendaItemId, 'votes'] as const,
    myConvocations: () => ['me', 'convocations'] as const,
  },
  notifications: {
    list: (page: number, size: number, unreadOnly: boolean) =>
      ['notifications', 'list', page, size, unreadOnly] as const,
    unreadCount: () => ['notifications', 'unread-count'] as const,
  },
};
