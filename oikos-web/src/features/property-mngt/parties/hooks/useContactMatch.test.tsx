import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { useContactMatch } from '@/features/property-mngt/parties/hooks/useContactMatch';
import { useParties } from '@/features/property-mngt/parties/hooks/useParties';
import { getContactByAccountEmail } from '@/features/property-mngt/properties/api/getContactByAccountEmail';

vi.mock('@/features/property-mngt/parties/hooks/useParties', () => ({ useParties: vi.fn() }));
vi.mock('@/features/property-mngt/properties/api/getContactByAccountEmail', () => ({
  getContactByAccountEmail: vi.fn(),
}));

let queryClient: QueryClient;

function wrapper({ children }: { children: ReactNode }) {
  return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
}

interface KnownParty {
  id: string;
  fullName: string;
  email: string | null;
  phone: string | null;
}

function directoryHolds(parties: KnownParty[]) {
  vi.mocked(useParties).mockReturnValue({
    data: { content: parties, page: 0, size: 5, totalElements: parties.length, totalPages: 1 },
    isLoading: false,
    isError: false,
  } as never);
}

function render(email?: string, phone?: string) {
  return renderHook(() => useContactMatch('p-1', email, phone), { wrapper });
}

beforeEach(() => {
  vi.clearAllMocks();
  queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  directoryHolds([]);
  vi.mocked(getContactByAccountEmail).mockResolvedValue(null);
  // Le hook attend 300 ms avant de chercher : le test n'attend pas pour de vrai.
  vi.useFakeTimers({ shouldAdvanceTime: true });
});

/**
 * Trois filets, dans l'ordre exact où le serveur les pose - un écran qui
 * annoncerait une fiche et un serveur qui en retiendrait une autre serait pire
 * que pas de rapprochement du tout.
 */
describe('useContactMatch', () => {
  it("reconnaît d'abord une fiche portant cet email", async () => {
    directoryHolds([{ id: 'party-1', fullName: 'Jane Doe', email: 'jane.doe@example.com', phone: null }]);

    const { result } = render('jane.doe@example.com');

    await waitFor(() => expect(result.current?.matchedOn).toBe('EMAIL'));
    expect(result.current?.partyId).toBe('party-1');
  });

  it('reconnaît ensuite une fiche portant ce téléphone', async () => {
    directoryHolds([{ id: 'party-2', fullName: 'Karim Alami', email: null, phone: '+212612345678' }]);

    const { result } = render(undefined, '+212612345678');

    await waitFor(() => expect(result.current?.matchedOn).toBe('PHONE'));
    expect(result.current?.partyId).toBe('party-2');
  });

  /**
   * Le cas qui fabriquait des doublons : l'adresse tapée est celle du compte,
   * la fiche en porte une autre, et rien dans l'annuaire ne correspond.
   */
  it("reconnaît enfin la personne à son adresse de connexion", async () => {
    vi.mocked(getContactByAccountEmail).mockResolvedValue({
      partyId: 'party-3',
      fullName: 'Karim Benali',
      email: 'user1.party@oikos.com',
    });

    const { result } = render('user1@oikos.com');

    await waitFor(() => expect(result.current?.matchedOn).toBe('ACCOUNT_EMAIL'));
    expect(result.current?.partyId).toBe('party-3');
    // La fiche garde son adresse : c'est elle que l'écran doit nommer.
    expect(result.current?.contactEmail).toBe('user1.party@oikos.com');
  });

  it("préfère la fiche à l'adresse de connexion quand les deux répondent", async () => {
    directoryHolds([{ id: 'party-1', fullName: 'Jane Doe', email: 'jane.doe@example.com', phone: null }]);
    vi.mocked(getContactByAccountEmail).mockResolvedValue({
      partyId: 'party-3',
      fullName: 'Quelqu’un d’autre',
      email: null,
    });

    const { result } = render('jane.doe@example.com');

    await waitFor(() => expect(result.current?.partyId).toBe('party-1'));
    expect(result.current?.matchedOn).toBe('EMAIL');
  });

  // Interroger les comptes sur « jane@ » ne rapporterait rien : le champ est
  // relu à chaque frappe, autant ne pas appeler pour rien.
  it("n'interroge pas les comptes sur une adresse en cours de saisie", async () => {
    const { result } = render('jane@');

    await waitFor(() => expect(result.current).toBeUndefined());
    expect(getContactByAccountEmail).not.toHaveBeenCalled();
  });

  it('ne reconnaît personne quand rien ne correspond', async () => {
    const { result } = render('inconnu@example.com');

    await waitFor(() => expect(getContactByAccountEmail).toHaveBeenCalled());
    expect(result.current).toBeUndefined();
  });
});
