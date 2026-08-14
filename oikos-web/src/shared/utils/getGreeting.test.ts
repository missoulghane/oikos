import { describe, expect, it } from 'vitest';
import { getGreeting } from '@/shared/utils/getGreeting';

describe('getGreeting', () => {
  it('returns Bonjour during the day', () => {
    expect(getGreeting(new Date(2026, 0, 1, 9))).toBe('Bonjour');
    expect(getGreeting(new Date(2026, 0, 1, 17, 59))).toBe('Bonjour');
  });

  it('returns Bonsoir in the evening and at night', () => {
    expect(getGreeting(new Date(2026, 0, 1, 18))).toBe('Bonsoir');
    expect(getGreeting(new Date(2026, 0, 1, 23))).toBe('Bonsoir');
    expect(getGreeting(new Date(2026, 0, 1, 4, 59))).toBe('Bonsoir');
  });
});
