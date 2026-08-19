import { describe, expect, it } from 'vitest';
import { composePhone, DEFAULT_DIAL_CODE, splitPhone } from '@/shared/components/PhoneField/dialCodes';

describe('splitPhone', () => {
  it('sépare indicatif et partie nationale', () => {
    expect(splitPhone('+212612345678')).toEqual({ iso: 'MA', nationalNumber: '612345678' });
  });

  it("retient l'indicatif le plus long", () => {
    // +1 est un préfixe de +212 : trié par longueur, le Maroc gagne.
    expect(splitPhone('+212612345678').iso).toBe('MA');
    expect(splitPhone('+15551234567').iso).toBe('US');
  });

  it("retombe sur l'indicatif par défaut pour un numéro sans indicatif connu", () => {
    expect(splitPhone('612345678')).toEqual({ iso: DEFAULT_DIAL_CODE.iso, nationalNumber: '612345678' });
  });

  it('traite une valeur absente comme une saisie vide', () => {
    // Un formulaire dont le défaut manque (brouillon d'une version antérieure)
    // rendait `undefined` ici, et l'écran entier tombait sur le `.trim()`.
    expect(splitPhone(undefined)).toEqual({ iso: DEFAULT_DIAL_CODE.iso, nationalNumber: '' });
    expect(splitPhone(null)).toEqual({ iso: DEFAULT_DIAL_CODE.iso, nationalNumber: '' });
    expect(splitPhone('')).toEqual({ iso: DEFAULT_DIAL_CODE.iso, nationalNumber: '' });
  });
});

describe('composePhone', () => {
  it("retire les zéros de tête, qui n'existent pas en format international", () => {
    expect(composePhone('+212', '0612345678')).toBe('+212612345678');
  });

  it('ne compose rien tant que la partie nationale est vide', () => {
    expect(composePhone('+212', '')).toBe('');
  });
});
