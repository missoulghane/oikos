import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Input } from '@/shared/components/Input/Input';

// La convention « obligatoire/facultatif » du produit tient à deux choses : une
// astérisque visible sur les champs requis (expliquée une fois par
// RequiredFieldsHint), et un nom de champ qui, lui, ne bouge pas - c'est par ce
// nom qu'on cherche « Email », à la voix comme au clavier.
describe('Input', () => {
  it('marks a required field with an asterisk kept out of its name', () => {
    render(<Input label="Email" name="email" required />);

    expect(screen.getByText('*')).toBeInTheDocument();
    expect(screen.getByLabelText('Email')).toBeRequired();
  });

  it('marks nothing on an optional field', () => {
    render(<Input label="Téléphone" name="phone" />);

    expect(screen.queryByText('*')).not.toBeInTheDocument();
    expect(screen.getByLabelText('Téléphone')).not.toBeRequired();
  });

  it('shows a fixed prefix inside the field', () => {
    render(<Input label="Numéro de lot" name="unitNumber" prefix="N°" />);

    expect(screen.getByText('N°')).toBeInTheDocument();
  });
});
