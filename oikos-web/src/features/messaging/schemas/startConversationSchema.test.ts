import { describe, expect, it } from 'vitest';
import { startConversationSchema } from '@/features/messaging/schemas/startConversationSchema';

describe('startConversationSchema', () => {
  it('accepts a valid payload', () => {
    const result = startConversationSchema.safeParse({ subject: 'Fuite d’eau', body: 'Bonjour à tous !' });

    expect(result.success).toBe(true);
  });

  it('rejects a blank subject', () => {
    const result = startConversationSchema.safeParse({ subject: '   ', body: 'Bonjour' });

    expect(result.success).toBe(false);
  });

  it('rejects a subject over 200 characters', () => {
    const result = startConversationSchema.safeParse({ subject: 'a'.repeat(201), body: 'Bonjour' });

    expect(result.success).toBe(false);
  });

  it('rejects a blank body', () => {
    const result = startConversationSchema.safeParse({ subject: 'Sujet', body: '   ' });

    expect(result.success).toBe(false);
  });
});
