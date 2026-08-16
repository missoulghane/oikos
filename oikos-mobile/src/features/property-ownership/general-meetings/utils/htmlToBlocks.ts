export type MinutesBlockType = 'heading' | 'subheading' | 'paragraph' | 'listItem';

export interface MinutesBlock {
  type: MinutesBlockType;
  text: string;
}

const BLOCK_PATTERN = /<(h1|h2|h3|p|li)\b[^>]*>([\s\S]*?)<\/\1>/gi;

const ENTITIES: Record<string, string> = {
  '&amp;': '&',
  '&lt;': '<',
  '&gt;': '>',
  '&quot;': '"',
  '&#39;': "'",
  '&nbsp;': ' ',
};

/**
 * Turns the minutes' HTML into blocks React Native can lay out.
 *
 * <p>React Native has no innerHTML, and pulling a WebView or an HTML renderer
 * in for one read-only screen would be a dependency for a paragraph. The
 * markup here is not arbitrary: it is produced by this project's own
 * MinutesComposer (headings, paragraphs, list items, some bold/italic), so a
 * small structural pass is enough - and it degrades to plain paragraphs rather
 * than breaking if that composer ever emits something new.
 *
 * <p>Not a general-purpose HTML parser, and deliberately not treated as one:
 * inline tags are stripped rather than rendered, and anything outside the five
 * block tags is dropped.
 */
export function htmlToBlocks(html: string): MinutesBlock[] {
  const blocks: MinutesBlock[] = [];
  for (const match of html.matchAll(BLOCK_PATTERN)) {
    const tag = match[1].toLowerCase();
    const text = stripInlineTags(match[2]);
    if (text === '') {
      continue;
    }
    blocks.push({ type: blockTypeOf(tag), text });
  }
  // No recognizable block: show the text rather than an empty screen.
  if (blocks.length === 0) {
    const fallback = stripInlineTags(html);
    return fallback === '' ? [] : [{ type: 'paragraph', text: fallback }];
  }
  return blocks;
}

function blockTypeOf(tag: string): MinutesBlockType {
  if (tag === 'h1') {
    return 'heading';
  }
  if (tag === 'h2' || tag === 'h3') {
    return 'subheading';
  }
  return tag === 'li' ? 'listItem' : 'paragraph';
}

function stripInlineTags(html: string): string {
  const withoutTags = html.replace(/<[^>]+>/g, '');
  const decoded = Object.entries(ENTITIES).reduce(
    (text, [entity, char]) => text.split(entity).join(char),
    withoutTags,
  );
  return decoded.replace(/\s+/g, ' ').trim();
}
