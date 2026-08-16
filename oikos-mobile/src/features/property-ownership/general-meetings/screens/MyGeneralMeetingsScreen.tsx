import { FlatList, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useMyConvocations } from '@/features/property-ownership/general-meetings/hooks/useMyConvocations';
import { useReplyToConvocation } from '@/features/property-ownership/general-meetings/hooks/useReplyToConvocation';
import { MyConvocationCard } from '@/features/property-ownership/general-meetings/components/MyConvocationCard';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Loader } from '@/shared/components/Loader/Loader';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type { HomeStackParamList } from '@/app/navigation/HomeStackNavigator';

type Props = NativeStackScreenProps<HomeStackParamList, 'MyGeneralMeetings'>;

export function MyGeneralMeetingsScreen({ navigation }: Props) {
  const convocations = useMyConvocations();
  const reply = useReplyToConvocation();

  return (
    <SafeAreaView style={styles.container} edges={['bottom']}>
      <FlatList
        data={convocations.data ?? []}
        keyExtractor={(convocation) => convocation.id}
        contentContainerStyle={styles.content}
        ListHeaderComponent={
          <View style={styles.header}>
            <Text style={styles.intro}>
              Indiquez votre présence pour chacun de vos lots, et consultez les procès-verbaux publiés.
            </Text>
            {reply.isError && <Alert message={getErrorMessage(reply.error)} />}
          </View>
        }
        renderItem={({ item }) => (
          <MyConvocationCard
            convocation={item}
            isReplying={reply.isPending}
            onPress={() =>
              navigation.navigate('MyGeneralMeetingDetail', {
                meetingId: item.generalMeetingId,
                title: item.meetingTitle,
              })
            }
            onReply={(attendanceReply) => reply.mutate({ convocationId: item.id, reply: attendanceReply })}
          />
        )}
        ItemSeparatorComponent={() => <View style={styles.separator} />}
        ListEmptyComponent={
          <>
            {convocations.isLoading && <Loader label="Chargement de mes assemblées…" />}
            {convocations.isError && <Alert message={getErrorMessage(convocations.error)} />}
            {convocations.isSuccess && (
              <EmptyState title="Aucune convocation">
                Vous serez convoqué(e) ici dès qu'une assemblée générale sera lancée sur l'une de vos
                copropriétés.
              </EmptyState>
            )}
          </>
        }
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.gray[50] },
  content: { padding: 16, gap: 12 },
  header: { gap: 12 },
  intro: { fontSize: 13, color: colors.gray[500] },
  separator: { height: 12 },
});
