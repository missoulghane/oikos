import { describe, expect, it } from 'vitest';
import { statusOf } from '@/features/property-mngt/board-members/components/BoardMemberStatusBadge';
import type { BoardMember } from '@/features/property-mngt/board-members/types/boardMember.types';

function member(overrides: Partial<BoardMember> = {}): BoardMember {
  return {
    id: 'bm-1',
    propertyId: 'p-1',
    partyId: 'party-1',
    partyFullName: 'Jane Doe',
    partyEmail: 'jane.doe@example.com',
    boardRole: 'MEMBER',
    status: 'ACTIVE',
    hasLinkedAccount: false,
    ...overrides,
  };
}

/**
 * Un seul statut par membre, là où la ligne empilait le siège à valider et la
 * liaison de compte. L'ordre de priorité est la règle : c'est lui qui décide ce
 * que le syndic lit en premier.
 */
describe('statusOf', () => {
  it('fait passer la validation du siège avant tout le reste', () => {
    // Même avec un compte actif et une invitation en cours : c'est le seul état
    // qui bloque un droit.
    expect(statusOf(member({ status: 'PENDING_VALIDATION', hasLinkedAccount: true }), true)).toBe('TO_VALIDATE');
  });

  it('annonce un compte actif avant une invitation', () => {
    expect(statusOf(member({ hasLinkedAccount: true }), true)).toBe('ACCOUNT_ACTIVE');
  });

  it('signale une invitation en attente', () => {
    expect(statusOf(member(), true)).toBe('INVITED');
  });

  it('dit qu’il n’y a rien eu quand il n’y a rien eu', () => {
    expect(statusOf(member(), false)).toBe('NOT_INVITED');
  });
});
