import { useEffect, useState } from 'react';
import { FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useMyDrafts } from '@/features/messaging/hooks/useMyDrafts';
import { useDeleteDraft } from '@/features/messaging/hooks/useDeleteDraft';
import { DraftListItem } from '@/features/messaging/components/DraftListItem';
import { Input } from '@/shared/components/Input/Input';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Button } from '@/shared/components/Button/Button';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type { MainStackParamList } from '@/app/navigation/MainNavigator';

type Props = NativeStackScreenProps<MainStackParamList, 'Drafts'>;

const SEARCH_DEBOUNCE_MS = 300;

export function DraftsListScreen({ navigation }: Props) {
  const [page, setPage] = useState(0);
  const [searchInput, setSearchInput] = useState('');
  const [search, setSearch] = useState('');

  useEffect(() => {
    const timeout = setTimeout(() => setSearch(searchInput.trim()), SEARCH_DEBOUNCE_MS);
    return () => clearTimeout(timeout);
  }, [searchInput]);

  const drafts = useMyDrafts(page, search || undefined);
  const deleteDraft = useDeleteDraft();
  const data = drafts.data;

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Brouillons</Text>
        <Button onPress={() => navigation.navigate('NewConversation', {})} style={styles.newButton}>
          Nouveau message
        </Button>
      </View>

      <View style={styles.searchRow}>
        <Input
          label="Rechercher"
          placeholder="Titre, contenu…"
          value={searchInput}
          onChangeText={(text) => {
            setSearchInput(text);
            setPage(0);
          }}
        />
      </View>

      <FlatList
        data={data?.content ?? []}
        keyExtractor={(draft) => draft.id}
        contentContainerStyle={styles.list}
        renderItem={({ item }) => (
          <DraftListItem
            draft={item}
            onOpen={() => navigation.navigate('NewConversation', { draftId: item.id })}
            onDelete={() => deleteDraft.mutate(item.id)}
            isDeleting={deleteDraft.isPending && deleteDraft.variables === item.id}
          />
        )}
        ItemSeparatorComponent={() => <View style={styles.separator} />}
        ListEmptyComponent={
          <>
            {drafts.isLoading && <Loader label="Chargement de vos brouillons…" />}
            {drafts.isError && <Alert message={getErrorMessage(drafts.error)} />}
            {data && data.content.length === 0 && (
              <EmptyState title="Aucun brouillon">Enregistrez un message en cours comme brouillon pour le retrouver ici.</EmptyState>
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
    justifyContent: 'space-between',
    gap: 8,
    padding: 16,
    paddingBottom: 8,
  },
  title: {
    fontSize: 18,
    fontWeight: '600',
    color: colors.gray[900],
  },
  newButton: {
    minHeight: 36,
    paddingVertical: 8,
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
