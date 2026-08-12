import { describe, it, expect } from 'vitest';
import { canManageProperties, singleManagedPropertyId, boardPropertyId, boardPropertyIds } from '@/features/identity/me/utils/access';
import type { CurrentUser } from '@/features/identity/me/types/me.types';

function userWith(roleByProperty: CurrentUser['roleByProperty']): CurrentUser {
  return {
    id: 'user-1',
    fullName: 'Jane Doe',
    email: 'jane@doe.com',
    phone: null,
    roles: ['ROLE_USER'],
    roleByProperty,
    verified: true,
    enabled: true,
    hasAvatar: false,
  };
}

describe('canManageProperties', () => {
  it('a bare PROPERTY_OWNER grant does not manage the property', () => {
    expect(canManageProperties(userWith({ 'prop-1': ['PROPERTY_OWNER'] }))).toBe(false);
  });

  it('a board admin grant manages the property', () => {
    expect(canManageProperties(userWith({ 'prop-1': ['PROPERTY_BOARD_ADMIN'] }))).toBe(true);
  });

  it('a manager member grant manages the property', () => {
    expect(canManageProperties(userWith({ 'prop-1': ['PROPERTY_MANAGER_MEMBER'] }))).toBe(true);
  });

  it('no property grant at all does not manage anything', () => {
    expect(canManageProperties(userWith({}))).toBe(false);
  });

  it('manages the property when owner and board member are both granted there', () => {
    expect(canManageProperties(userWith({ 'prop-1': ['PROPERTY_OWNER', 'PROPERTY_BOARD_MEMBER'] }))).toBe(true);
  });
});

describe('singleManagedPropertyId', () => {
  it('ignores a PROPERTY_OWNER-only grant and returns null', () => {
    expect(singleManagedPropertyId(userWith({ 'prop-1': ['PROPERTY_OWNER'] }))).toBeNull();
  });

  it('returns the one staff property, even alongside an owner grant on another property', () => {
    expect(
      singleManagedPropertyId(userWith({ 'prop-1': ['PROPERTY_OWNER'], 'prop-2': ['PROPERTY_BOARD_ADMIN'] })),
    ).toBe('prop-2');
  });

  it('returns null when staff on more than one property', () => {
    expect(
      singleManagedPropertyId(
        userWith({ 'prop-1': ['PROPERTY_BOARD_ADMIN'], 'prop-2': ['PROPERTY_MANAGER_ADMIN'] }),
      ),
    ).toBeNull();
  });

  it('still resolves when the one staff property also carries an owner grant on itself', () => {
    expect(
      singleManagedPropertyId(userWith({ 'prop-1': ['PROPERTY_OWNER', 'PROPERTY_BOARD_ADMIN'] })),
    ).toBe('prop-1');
  });
});

describe('boardPropertyIds / boardPropertyId', () => {
  it('returns every property with a board mandate, not just the first', () => {
    expect(
      boardPropertyIds(
        userWith({ 'prop-1': ['PROPERTY_BOARD_ADMIN'], 'prop-2': ['PROPERTY_BOARD_MEMBER'] }),
      ),
    ).toEqual(['prop-1', 'prop-2']);
  });

  it('excludes properties held only as owner or manager', () => {
    expect(
      boardPropertyIds(
        userWith({ 'prop-1': ['PROPERTY_OWNER'], 'prop-2': ['PROPERTY_MANAGER_ADMIN'] }),
      ),
    ).toEqual([]);
  });

  it('boardPropertyId falls back to the first mandate for single-mandate callers', () => {
    expect(
      boardPropertyId(userWith({ 'prop-1': ['PROPERTY_BOARD_ADMIN'], 'prop-2': ['PROPERTY_BOARD_MEMBER'] })),
    ).toBe('prop-1');
  });

  it('boardPropertyId returns null with no board mandate', () => {
    expect(boardPropertyId(userWith({ 'prop-1': ['PROPERTY_OWNER'] }))).toBeNull();
  });
});
