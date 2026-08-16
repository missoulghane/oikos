import { StyleSheet, Text, View } from 'react-native';
import { colors } from '@/shared/theme/colors';
import { htmlToBlocks } from '@/features/property-ownership/general-meetings/utils/htmlToBlocks';

/**
 * Renders rich-text HTML as native text blocks - see htmlToBlocks for why this
 * app converts rather than embeds a WebView.
 *
 * <p>Used by the minutes and by a meeting's comment. Both are HTML written or
 * composed elsewhere; neither is trusted enough to be handed to anything that
 * would interpret it, which is exactly what the conversion avoids.
 */
export function RichTextContent({ html }: { html: string }) {
  const blocks = htmlToBlocks(html);

  return (
    <View style={styles.container}>
      {blocks.map((block, index) => {
        if (block.type === 'listItem') {
          return (
            <Text key={index} style={styles.listItem}>
              {'•'} {block.text}
            </Text>
          );
        }
        return (
          <Text key={index} style={styles[block.type]}>
            {block.text}
          </Text>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { gap: 6 },
  heading: { fontSize: 16, fontWeight: '600', color: colors.gray[900] },
  subheading: { marginTop: 8, fontSize: 14, fontWeight: '600', color: colors.gray[900] },
  paragraph: { fontSize: 14, lineHeight: 20, color: colors.gray[700] },
  listItem: { marginLeft: 8, fontSize: 14, lineHeight: 20, color: colors.gray[700] },
});
