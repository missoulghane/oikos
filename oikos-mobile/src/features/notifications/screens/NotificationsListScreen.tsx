import { useState } from 'react';
import { FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useCurrentUser, isOwnerOnProperty } from '@/features/identity/me';
import { useMyNotifications } from '@/features/notifications/hooks/useMyNotifications';
import { useMarkNotificationRead } from '@/features/notifications/hooks/useMarkNotificationRead';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { formatRelativeTime } from '@/shared/utils/formatRelativeTime';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type { MainStackParamList } from '@/app/navigation/MainNavigator';
import type { Notification } from '@/features/notifications/types/notification.types';

type Props = NativeStackScreenProps<MainStackParamList, 'Notifications'>;

// 'ALL' and 'OWNED' only - oikos-web adds one tab per board mandate
// (boardPropertyIds + useMandateProperties, which fetches property names via
// property-mngt's useProperties). property-mngt is out of scope on mobile
// (see PLAN.md), so those per-mandate tabs are dropped; the copropriétaire
// scope (Tout/Mes lots) is what this app's audience actually needs.
type Scope = 'ALL' | 'OWNED';

const PAGE_SIZE = 20;

export function NotificationsListScreen({ navigation }: Props) {
  const currentUser = useCurrentUser();
  const [page, setPage] = useState(0);
  const [scope, setScope] = useState<Scope>('ALL');
  const notifications = useMyNotifications(page, PAGE_SIZE);
  const markRead = useMarkNotificationRead();

  const data = notifications.data;
  const user = currentUser.data;

  function matchesScope(notification: Notification): boolean {
    if (scope === 'ALL' || !user) {
      return true;
    }
    if (!notification.propertyId) {
      return false;
    }
    return isOwnerOnProperty(user, notification.propertyId);
  }

  function openNotification(notification: Notification) {
    if (!notification.read) {
      markRead.mutate(notification.id);
    }
    // Only linkPath prefixes with a mobile screen behind them are handled -
    // property-mngt-bound links (e.g. REQUEST_RECEIVED's board screen) have
    // nowhere to navigate to yet on mobile, so they just mark as read.
    if (notification.linkPath?.startsWith('/property-ownership/installments')) {
      navigation.navigate('MainTabs', { screen: 'HomeTab', params: { screen: 'MyInstallments' } });
      return;
    }
    // GENERAL_MEETING_CALLED and the published-minutes notice both land on the
    // meeting itself - the id is the last path segment.
    if (notification.linkPath?.startsWith('/property-ownership/general-meetings/')) {
      const meetingId = notification.linkPath.split('/').filter(Boolean).pop();
      if (meetingId) {
        navigation.navigate('MainTabs', {
          screen: 'HomeTab',
          params: { screen: 'MyGeneralMeetingDetail', params: { meetingId } },
        });
      }
    }
  }

  const visibleContent = (data?.content ?? []).filter(matchesScope);

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.tabs}>
        {(['ALL', 'OWNED'] as const).map((tab) => (
          <Pressable key={tab} onPress={() => setScope(tab)} style={[styles.tab, scope === tab && styles.tabActive]}>
            <Text style={[styles.tabText, scope === tab && styles.tabTextActive]}>
              {tab === 'ALL' ? 'Tout' : 'Mes lots'}
            </Text>
          </Pressable>
        ))}
      </View>

      <FlatList
        data={visibleContent}
        keyExtractor={(notification) => notification.id}
        contentContainerStyle={styles.content}
        renderItem={({ item }) => (
          <Pressable onPress={() => openNotification(item)} style={styles.row}>
            <View style={styles.rowHeader}>
              <View style={styles.rowTitleGroup}>
                {!item.read && <View style={styles.unreadDot} />}
                <Text style={item.read ? styles.rowTitleRead : styles.rowTitleUnread} numberOfLines={1}>
                  {item.title}
                </Text>
              </View>
              <Text style={styles.rowTime}>{formatRelativeTime(item.createdAt)}</Text>
            </View>
            {item.body && (
              <Text style={styles.rowBody} numberOfLines={1}>
                {item.body}
              </Text>
            )}
          </Pressable>
        )}
        ItemSeparatorComponent={() => <View style={styles.separator} />}
        ListEmptyComponent={
          <>
            {notifications.isLoading && <Loader label="Chargement des notifications…" />}
            {notifications.isError && <Alert message={getErrorMessage(notifications.error)} />}
            {data && visibleContent.length === 0 && (
              <EmptyState title="Aucune notification">
                {scope === 'ALL'
                  ? 'Vous serez notifié ici des événements qui vous concernent (échéances, assemblées, demandes…).'
                  : 'Aucune notification dans cette catégorie pour le moment.'}
              </EmptyState>
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
  tabs: {
    flexDirection: 'row',
    gap: 8,
    paddingHorizontal: 16,
    paddingTop: 16,
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
  content: {
    paddingHorizontal: 16,
    flexGrow: 1,
  },
  row: {
    gap: 2,
    paddingVertical: 10,
  },
  rowHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 8,
  },
  rowTitleGroup: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  unreadDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: colors.brand[500],
  },
  rowTitleRead: {
    flex: 1,
    fontSize: 14,
    fontWeight: '500',
    color: colors.gray[700],
  },
  rowTitleUnread: {
    flex: 1,
    fontSize: 14,
    fontWeight: '700',
    color: colors.gray[900],
  },
  rowTime: {
    fontSize: 12,
    color: colors.gray[400],
  },
  rowBody: {
    fontSize: 13,
    color: colors.gray[500],
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
