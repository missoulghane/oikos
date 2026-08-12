import type { ComponentProps } from 'react';
import { Controller, type Control, type FieldPath, type FieldValues } from 'react-hook-form';
import { Input } from '@/shared/components/Input/Input';

// react-hook-form's `register` targets DOM refs; React Native inputs need this
// Controller-based binding instead. Shared across every form in the app so
// each feature doesn't redefine the same adapter (see LoginForm's original,
// now-inlined-nowhere-else version).
export function ControlledInput<TFieldValues extends FieldValues>({
  control,
  name,
  ...inputProps
}: {
  control: Control<TFieldValues>;
  name: FieldPath<TFieldValues>;
} & Omit<ComponentProps<typeof Input>, 'value' | 'onChangeText'>) {
  return (
    <Controller
      control={control}
      name={name}
      render={({ field: { onChange, onBlur, value } }) => (
        <Input value={value ?? ''} onChangeText={onChange} onBlur={onBlur} {...inputProps} />
      )}
    />
  );
}
