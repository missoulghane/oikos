import { useState } from 'react';
import { Controller, type Control, type FieldValues, type Path } from 'react-hook-form';
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { colors } from '@/shared/theme/colors';

interface MessageBodyEditorProps<TFieldValues extends FieldValues> {
  control: Control<TFieldValues>;
  name: Path<TFieldValues>;
  label: string;
  placeholder: string;
  numberOfLines: number;
  editable?: boolean;
  errorMessage?: string;
}

// Deliberately not a rich-text editor: the body stays a plain string on the
// wire (@Size(max = 4000), see sendMessageSchema) carrying a tiny
// markdown-lite syntax - "**bold**" and "- " bullet lines - that these two
// toolbar buttons insert at the current selection, and that
// renderMessageBody (MessageThreadItem) turns back into bold/bulleted Text
// on display. Kept in sync with oikos-web's MessageBodyEditor.tsx.
//
// `selection` is a controlled TextInput prop here (tracked via
// onSelectionChange) purely so the toolbar buttons can know/restore the
// cursor position after inserting markup - it stays in sync with normal
// typing since onSelectionChange fires on every user-driven change too.
export function MessageBodyEditor<TFieldValues extends FieldValues>({
  control,
  name,
  label,
  placeholder,
  numberOfLines,
  editable = true,
  errorMessage,
}: MessageBodyEditorProps<TFieldValues>) {
  const [selection, setSelection] = useState({ start: 0, end: 0 });

  return (
    <Controller
      control={control}
      name={name}
      render={({ field: { onChange, onBlur, value } }) => {
        const text: string = value ?? '';

        function applyBold() {
          const { start, end } = selection;
          const selected = text.slice(start, end);
          const next = `${text.slice(0, start)}**${selected}**${text.slice(end)}`;
          onChange(next);
          const cursor = start + 2 + selected.length;
          setSelection({ start: cursor, end: cursor });
        }

        function applyBulletList() {
          const { start, end } = selection;
          const lineStart = text.lastIndexOf('\n', start - 1) + 1;
          const nextNewline = text.indexOf('\n', end);
          const lineEnd = nextNewline === -1 ? text.length : nextNewline;
          const block = text.slice(lineStart, lineEnd);
          const newBlock = block
            .split('\n')
            .map((line) => (line.startsWith('- ') ? line : `- ${line}`))
            .join('\n');
          const next = text.slice(0, lineStart) + newBlock + text.slice(lineEnd);
          onChange(next);
          const cursor = lineStart + newBlock.length;
          setSelection({ start: cursor, end: cursor });
        }

        return (
          <View style={styles.container}>
            <Text style={styles.label}>{label}</Text>
            <View style={styles.toolbar} accessibilityRole="toolbar">
              <Pressable
                disabled={!editable}
                onPress={applyBold}
                style={styles.toolbarButton}
                accessibilityLabel="Gras"
                hitSlop={4}
              >
                <Text style={styles.boldIcon}>G</Text>
              </Pressable>
              <Pressable
                disabled={!editable}
                onPress={applyBulletList}
                style={styles.toolbarButton}
                accessibilityLabel="Liste à puces"
                hitSlop={4}
              >
                <Text style={styles.bulletIcon}>•</Text>
              </Pressable>
            </View>
            <TextInput
              value={text}
              onChangeText={onChange}
              onBlur={onBlur}
              onSelectionChange={(e) => setSelection(e.nativeEvent.selection)}
              selection={selection}
              placeholder={placeholder}
              placeholderTextColor={colors.gray[400]}
              editable={editable}
              multiline
              numberOfLines={numberOfLines}
              style={[styles.input, Boolean(errorMessage) && styles.inputError]}
            />
            {errorMessage && <Text style={styles.error}>{errorMessage}</Text>}
          </View>
        );
      }}
    />
  );
}

const styles = StyleSheet.create({
  container: {
    gap: 4,
  },
  label: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.gray[700],
  },
  toolbar: {
    flexDirection: 'row',
    gap: 4,
  },
  toolbarButton: {
    width: 28,
    height: 28,
    borderRadius: 6,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.gray[100],
  },
  boldIcon: {
    fontSize: 14,
    fontWeight: '700',
    color: colors.gray[600],
  },
  bulletIcon: {
    fontSize: 16,
    color: colors.gray[600],
  },
  input: {
    minHeight: 44,
    borderWidth: 1,
    borderColor: colors.gray[300],
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 10,
    fontSize: 16,
    color: colors.gray[800],
    textAlignVertical: 'top',
  },
  inputError: {
    borderColor: colors.error[500],
  },
  error: {
    fontSize: 14,
    color: colors.error[500],
  },
});
