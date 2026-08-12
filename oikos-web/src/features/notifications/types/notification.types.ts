import type { Paged } from '@/shared/types/pagination.types';

export type NotificationType =
  | 'INSTALLMENT_OVERDUE'
  | 'GENERAL_MEETING_CALLED'
  | 'RELAUNCH_TO_VALIDATE'
  | 'REQUEST_RECEIVED'
  | 'GENERAL';

export interface Notification {
  id: string;
  propertyId: string | null;
  type: NotificationType;
  title: string;
  body: string | null;
  /** In-app relative path to navigate to when opened (e.g. "/property-ownership/installments") -
   * a display hint, not validated against the route table. Null for a notification with nowhere
   * specific to send the reader. */
  linkPath: string | null;
  read: boolean;
  createdAt: string; // ISO instant
}

export type PagedNotifications = Paged<Notification>;

export interface UnreadNotificationCount {
  unreadCount: number;
}
