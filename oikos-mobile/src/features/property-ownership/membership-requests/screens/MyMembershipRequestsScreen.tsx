import { FlatList, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useMyMembershipRequests } from '@/features/property-ownership/membership-requests/hooks/useMyMembershipRequests';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type {
  OwnedMembershipRequest,
  OwnedMembershipRequestStatus,
} from '@/features/property-ownership/membership-requests/types/membershipRequest.types';

const STATUS_BADGE: Record<OwnedMembershipRequestStatus, { label: string; color: 'success' | 'warning' | 'error' }> = {
  PENDING: { label: 'En attente', color: 'warning' },
  ACCEPTED: { label: 'Acceptée', color: 'success' },
  REJECTED: { label: 'Refusée', color: 'error' },
};

export function MyMembershipRequestsScreen() {
  const requests = useMyMembershipRequests();

  return (
    <SafeAreaView style={styles.container}>
      <FlatList
        data={requests.data ?? []}
        keyExtractor={(request) => request.id}
        contentContainerStyle={styles.content}
        renderItem={({ item }: { item: OwnedMembershipRequest }) => {
          const badge = STATUS_BADGE[item.status];
          return (
            <View style={styles.row}>
              <View style={styles.rowHeader}>
                <Text style={styles.rowLabel}>
                  {item.propertyName ?? 'Copropriété'}
                  {item.unitNumber && ` — Lot ${item.unitNumber}`}
                  {item.unitTypeName && ` (${item.unitTypeName})`}
                </Text>
                <Badge color={badge.color}>{badge.label}</Badge>
              </View>
              {item.status === 'REJECTED' && item.rejectionReason && (
                <Text style={styles.reason}>Motif : {item.rejectionReason}</Text>
              )}
            </View>
          );
        }}
        ItemSeparatorComponent={() => <View style={styles.separator} />}
        ListEmptyComponent={
          <>
            {requests.isLoading && <Loader label="Chargement de vos demandes…" />}
            {requests.isError && <Alert message={getErrorMessage(requests.error)} />}
            {requests.isSuccess && (
              <EmptyState title="Aucune demande d'adhésion">
                Vos demandes d'adhésion soumises via un lien d'invitation apparaîtront ici.
              </EmptyState>
            )}
          </>
        }
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.gray[50],
  },
  content: {
    padding: 16,
    flexGrow: 1,
  },
  row: {
    gap: 4,
    paddingVertical: 4,
  },
  rowHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 12,
  },
  rowLabel: {
    flex: 1,
    fontSize: 14,
    color: colors.gray[700],
  },
  reason: {
    fontSize: 12,
    color: colors.gray[400],
  },
  separator: {
    height: 1,
    backgroundColor: colors.gray[100],
    marginVertical: 8,
  },
});
