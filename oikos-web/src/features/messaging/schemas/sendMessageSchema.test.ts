import { describe, expect, it } from 'vitest';
import { sendMessageSchema } from '@/features/messaging/schemas/sendMessageSchema';

describe('sendMessageSchema', () => {
  it('accepts a valid payload', () => {
    const result = sendMessageSchema.safeParse({ body: 'Bonjour à tous !' });

    expect(result.success).toBe(true);
  });

  it('rejects a blank body', () => {
    const result = sendMessageSchema.safeParse({ body: '   ' });

    expect(result.success).toBe(false);
  });

  it('rejects a body over 4000 characters', () => {
    const result = sendMessageSchema.safeParse({ body: 'a'.repeat(4001) });

    expect(result.success).toBe(false);
  });

  it('accepts a body at exactly 4000 characters', () => {
    const result = sendMessageSchema.safeParse({ body: 'a'.repeat(4000) });

    expect(result.success).toBe(true);
  });
});
