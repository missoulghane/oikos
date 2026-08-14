import { describe, expect, it } from 'vitest';
import { getFirstName } from '@/shared/utils/getFirstName';

describe('getFirstName', () => {
  it('returns the first token of a full name', () => {
    expect(getFirstName('Salma Idrissi')).toBe('Salma');
  });

  it('returns the whole string when there is no space', () => {
    expect(getFirstName('Salma')).toBe('Salma');
  });

  it('ignores leading/trailing whitespace and repeated spaces', () => {
    expect(getFirstName('  Salma   Idrissi  ')).toBe('Salma');
  });
});
