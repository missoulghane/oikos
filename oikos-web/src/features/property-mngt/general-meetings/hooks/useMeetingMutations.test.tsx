import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import {
  useRecordConvocationDelivery,
  useReplyToConvocation,
} from '@/features/property-mngt/general-meetings/hooks/useMeetingMutations';
import { recordConvocationDelivery } from '@/features/property-mngt/general-meetings/api/recordConvocationDelivery';
import { replyToConvocation } from '@/features/property-mngt/general-meetings/api/replyToConvocation';

vi.mock('@/features/property-mngt/general-meetings/api/recordConvocationDelivery', () => ({
  recordConvocationDelivery: vi.fn(),
}));
vi.mock('@/features/property-mngt/general-meetings/api/replyToConvocation', () => ({
  replyToConvocation: vi.fn(),
}));

const mockedRecordDelivery = vi.mocked(recordConvocationDelivery);
const mockedReply = vi.mocked(replyToConvocation);

let queryClient: QueryClient;

function wrapper({ children }: { children: ReactNode }) {
  return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
}

beforeEach(() => {
  vi.clearAllMocks();
  queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  mockedRecordDelivery.mockResolvedValue({} as never);
  mockedReply.mockResolvedValue({} as never);
});

/**
 * A convocation's own detail is fetched under ['convocations', id] rather than
 * under its meeting's key, because that single fetch carries the confirmation
 * code the list deliberately never does. The meeting-scoped invalidation used to
 * miss it entirely, so recording a delivery or an answer left the detail page
 * showing yesterday's history until the syndic reloaded the page by hand.
 *
 * <p>Asserted on the query cache rather than through the page: what broke was
 * which keys get invalidated, and a render test would pass just as happily with
 * the page refetching for some unrelated reason.
 */
describe('the meeting-scoped invalidation', () => {
  it('refreshes a convocation detail after a delivery is recorded', async () => {
    const detail = ['convocations', 'convocation-1'];
    queryClient.setQueryData(detail, { id: 'convocation-1' });
    const invalidate = vi.spyOn(queryClient, 'invalidateQueries');

    const { result } = renderHook(() => useRecordConvocationDelivery('meeting-1'), { wrapper });
    result.current.mutate({ convocationId: 'convocation-1', channel: 'POSTAL_MAIL', deliveryStatus: 'SENT' });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(invalidate).toHaveBeenCalledWith({ queryKey: ['convocations'] });
  });

  it('refreshes it after an answer is recorded too', async () => {
    const invalidate = vi.spyOn(queryClient, 'invalidateQueries');

    const { result } = renderHook(() => useReplyToConvocation('meeting-1'), { wrapper });
    result.current.mutate({ convocationId: 'convocation-1', attendanceReply: 'ATTENDING' });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(invalidate).toHaveBeenCalledWith({ queryKey: ['convocations'] });
  });

  it('still invalidates the meeting and the list it hangs off', async () => {
    // The tracking table carries the same facts one screen up, and the list carries
    // the status badge - neither must be traded away for the detail.
    const invalidate = vi.spyOn(queryClient, 'invalidateQueries');

    const { result } = renderHook(() => useReplyToConvocation('meeting-1'), { wrapper });
    result.current.mutate({ convocationId: 'convocation-1', attendanceReply: 'NOT_ATTENDING' });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(invalidate).toHaveBeenCalledWith({ queryKey: ['general-meetings', 'meeting-1'] });
    expect(invalidate).toHaveBeenCalledWith({ queryKey: ['general-meetings'] });
  });
});
