import { httpClient } from '@/shared/api/httpClient';
import type { SortDirection } from '@/shared/utils/sorting';
import type {
  MembershipRequestSortField,
  MembershipRequestStatus,
  PagedMembershipRequests,
} from '@/features/property-mngt/invitations/types/invitation.types';

export interface GetMembershipRequestsParams {
  propertyId: string;
  page: number;
  size: number;
  search?: string;
  status?: MembershipRequestStatus;
  sortBy?: MembershipRequestSortField;
  sortDirection?: SortDirection;
}

export async function getMembershipRequests({
  propertyId,
  ...params
}: GetMembershipRequestsParams): Promise<PagedMembershipRequests> {
  const { data } = await httpClient.get<PagedMembershipRequests>(`/properties/${propertyId}/membership-requests`, {
    params,
  });
  return data;
}
