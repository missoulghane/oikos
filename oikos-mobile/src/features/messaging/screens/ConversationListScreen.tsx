import { useEffect, useState } from 'react';
import { FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useMyConversations } from '@/features/messaging/hooks/useMyConversations';
import { ConversationListItem } from '@/features/messaging/components/ConversationListItem';
import { Input } from '@/shared/components/Input/Input';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Button } from '@/shared/components/Button/Button';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { BOX_LABEL, BOX_EMPTY_STATE } from '@/features/messaging/utils/boxPath';
import { colors } from '@/shared/theme/colors';
import type { MessagingStackParamList } from '@/app/navigation/MessagingStackNavigator';
import type { ConversationBox } from '@/features/messaging/types/messaging.types';

type Props = NativeStackScreenProps<MessagingStackParamList, 'ConversationList'>;

const SEARCH_DEBOUNCE_MS = 300;

export function ConversationListScreen({ navigation }: Props) {
  const [box, setBox] = useState<ConversationBox>('RECEIVED');
  const [page, setPage] = useState(0);
  const [searchInput, setSearchInput] = useState('');
  const [search, setSearch] = useState('');

  useEffect(() => {
    const timeout = setTimeout(() => setSearch(searchInput.trim()), SEARCH_DEBOUNCE_MS);
    return () => clearTimeout(timeout);
  }, [searchInput]);

  // Title depends on `box`, a local state - can't be a static navigator
  // `options`, and setOptions merges shallowly so it won't clobber the
  // static `headerRight` bell set on this screen's registration.
  useEffect(() => {
    navigation.setOptions({ title: BOX_LABEL[box] });
  }, [box, navigation]);

  const conversations = useMyConversations(page, box, search || undefined);
  const data = conversations.data;

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.header}>
        <Button onPress={() => navigation.navigate('NewConversation', {})} style={styles.newButton}>
          Nouveau message
        </Button>
      </View>

      <View style={styles.tabs}>
        {(['RECEIVED', 'SENT'] as const).map((tab) => (
          <Pressable
            key={tab}
            onPress={() => {
              setBox(tab);
              setPage(0);
            }}
            style={[styles.tab, box === tab && styles.tabActive]}
          >
            <Text style={[styles.tabText, box === tab && styles.tabTextActive]}>{BOX_LABEL[tab]}</Text>
          </Pressable>
        ))}
        <Pressable onPress={() => navigation.navigate('Drafts')} style={styles.draftsLink}>
          <Text style={styles.draftsLinkText}>Brouillons</Text>
        </Pressable>
      </View>

      <View style={styles.searchRow}>
        <Input
          label="Rechercher"
          placeholder="Nom, copropriété…"
          value={searchInput}
          onChangeText={(text) => {
            setSearchInput(text);
            setPage(0);
          }}
        />
      </View>

      <FlatList
        data={data?.content ?? []}
        keyExtractor={(conversation) => conversation.id}
        contentContainerStyle={styles.list}
        renderItem={({ item }) => (
          <ConversationListItem
            conversation={item}
            onPress={() => navigation.navigate('Conversation', { conversation: item, box })}
          />
        )}
        ItemSeparatorComponent={() => <View style={styles.separator} />}
        ListEmptyComponent={
          <>
            {conversations.isLoading && <Loader label="Chargement de vos conversations…" />}
            {conversations.isError && <Alert message={getErrorMessage(conversations.error)} />}
            {data && data.content.length === 0 && (
              <EmptyState title={BOX_EMPTY_STATE[box]}>Envoyez un nouveau message avec le bouton ci-dessus.</EmptyState>
            )}
          </>
        }
      />

      {data && data.totalElements > 0 && (
        <View style={styles.pagination}>
          <Text style={styles.paginationText}>
            Page {data.pageNumber + 1} sur {data.totalPages}
          </Text>
          <View style={styles.paginationButtons}>
            <Pressable
              disabled={data.pageNumber <= 0}
              onPress={() => setPage((current) => current - 1)}
              style={[styles.paginationButton, data.pageNumber <= 0 && styles.paginationButtonDisabled]}
            >
              <Text style={styles.paginationButtonText}>‹ Précédent</Text>
            </Pressable>
            <Pressable
              disabled={data.pageNumber + 1 >= data.totalPages}
              onPress={() => setPage((current) => current + 1)}
              style={[styles.paginationButton, data.pageNumber + 1 >= data.totalPages && styles.paginationButtonDisabled]}
            >
              <Text style={styles.paginationButtonText}>Suivant ›</Text>
            </Pressable>
          </View>
        </View>
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.gray[50],
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'flex-end',
    gap: 8,
    padding: 16,
    paddingBottom: 8,
  },
  newButton: {
    minHeight: 36,
    paddingVertical: 8,
  },
  tabs: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    paddingHorizontal: 16,
    paddingBottom: 8,
  },
  tab: {
    borderRadius: 999,
    paddingHorizontal: 12,
    paddingVertical: 6,
  },
  tabActive: {
    backgroundColor: colors.brand[500],
  },
  tabText: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.gray[600],
  },
  tabTextActive: {
    color: colors.white,
  },
  draftsLink: {
    marginLeft: 'auto',
    paddingHorizontal: 8,
    paddingVertical: 6,
  },
  draftsLinkText: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.brand[500],
  },
  searchRow: {
    paddingHorizontal: 16,
    paddingBottom: 8,
  },
  list: {
    paddingHorizontal: 16,
    flexGrow: 1,
  },
  separator: {
    height: 1,
    backgroundColor: colors.gray[100],
  },
  pagination: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderTopWidth: 1,
    borderTopColor: colors.gray[200],
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  paginationText: {
    fontSize: 12,
    color: colors.gray[500],
  },
  paginationButtons: {
    flexDirection: 'row',
    gap: 16,
  },
  paginationButton: {
    paddingVertical: 4,
  },
  paginationButtonDisabled: {
    opacity: 0.4,
  },
  paginationButtonText: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.brand[500],
  },
});
