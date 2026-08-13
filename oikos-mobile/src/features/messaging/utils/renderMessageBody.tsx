import type { ReactNode } from 'react';
import type { TextStyle } from 'react-native';
import { StyleSheet, Text, View } from 'react-native';

// Matches the markdown-lite syntax MessageBodyEditor's toolbar inserts:
// "**bold**" spans and "- " bullet lines. Kept in sync with oikos-web's
// renderMessageBody.tsx so a message written on either client displays the
// same way on both.
function renderInline(text: string, keyPrefix: string, textStyle: TextStyle): ReactNode {
  const parts = text.split(/(\*\*[^*]+\*\*)/g).filter((part) => part !== '');
  return parts.map((part, index) =>
    part.startsWith('**') && part.endsWith('**') && part.length > 4 ? (
      <Text key={`${keyPrefix}-${index}`} style={[textStyle, styles.bold]}>
        {part.slice(2, -2)}
      </Text>
    ) : (
      <Text key={`${keyPrefix}-${index}`} style={textStyle}>
        {part}
      </Text>
    ),
  );
}

export function renderMessageBody(body: string, textStyle: TextStyle): ReactNode {
  const lines = body.split('\n');
  const blocks: ReactNode[] = [];
  let textBuffer: string[] = [];
  let bulletBuffer: string[] = [];

  function flushText(key: string) {
    if (textBuffer.length === 0) {
      return;
    }
    blocks.push(
      <Text key={key} style={textStyle}>
        {textBuffer.map((line, index) => (
          <Text key={index}>
            {renderInline(line, `${key}-${index}`, textStyle)}
            {index < textBuffer.length - 1 ? '\n' : ''}
          </Text>
        ))}
      </Text>,
    );
    textBuffer = [];
  }

  function flushBullets(key: string) {
    if (bulletBuffer.length === 0) {
      return;
    }
    blocks.push(
      <View key={key} style={styles.bulletList}>
        {bulletBuffer.map((line, index) => (
          <View key={index} style={styles.bulletRow}>
            <Text style={textStyle}>{'• '}</Text>
            <Text style={[textStyle, styles.bulletText]}>{renderInline(line, `${key}-${index}`, textStyle)}</Text>
          </View>
        ))}
      </View>,
    );
    bulletBuffer = [];
  }

  lines.forEach((line, index) => {
    if (line.startsWith('- ')) {
      flushText(`p-${index}`);
      bulletBuffer.push(line.slice(2));
    } else {
      flushBullets(`ul-${index}`);
      textBuffer.push(line);
    }
  });
  flushText('p-end');
  flushBullets('ul-end');

  return <>{blocks}</>;
}

// Preview-only cleanup (conversation list row, see ConversationListItem) -
// strips the markdown-lite markers so "**Réunion** demain\n- 18h" reads as
// plain text rather than showing literal asterisks/dashes.
export function stripMessageBodyMarkup(text: string): string {
  return text.replace(/\*\*(.+?)\*\*/g, '$1').replace(/^[ \t]*-\s+/gm, '');
}

const styles = StyleSheet.create({
  bold: {
    fontWeight: '700',
  },
  bulletList: {
    gap: 2,
  },
  bulletRow: {
    flexDirection: 'row',
  },
  bulletText: {
    flex: 1,
  },
});
