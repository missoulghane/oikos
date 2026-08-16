import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useGeneralMeeting } from '@/features/property-ownership/general-meetings/hooks/useGeneralMeeting';
import { useAgendaItems } from '@/features/property-ownership/general-meetings/hooks/useAgendaItems';
import { isNotFound, useMeetingMinutes } from '@/features/property-ownership/general-meetings/hooks/useMeetingMinutes';
import { AgendaItemResultSummary } from '@/features/property-ownership/general-meetings/components/AgendaItemResultSummary';
import { RichTextContent } from '@/features/property-ownership/general-meetings/components/RichTextContent';
import {
  MAJORITY_RULE_LABELS,
  MEETING_STATUS_BADGE_COLORS,
  MEETING_STATUS_LABELS,
  MEETING_TYPE_LABELS,
} from '@/features/property-ownership/general-meetings/constants/generalMeetingLabels';
import { formatScheduledAt, formatVenue } from '@/features/property-ownership/general-meetings/utils/formatMeeting';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type { HomeStackParamList } from '@/app/navigation/HomeStackNavigator';

type Props = NativeStackScreenProps<HomeStackParamList, 'MyGeneralMeetingDetail'>;

function DetailRow({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.detailRow}>
      <Text style={styles.detailLabel}>{label}</Text>
      <Text style={styles.detailValue}>{value}</Text>
    </View>
  );
}

export function MyGeneralMeetingDetailScreen({ route }: Props) {
  const { meetingId } = route.params;
  const meeting = useGeneralMeeting(meetingId);
  const agenda = useAgendaItems(meetingId);
  const minutes = useMeetingMinutes(meetingId);

  if (meeting.isLoading) {
    return (
      <SafeAreaView style={styles.container} edges={['bottom']}>
        <Loader label="Chargement de l'assemblée…" />
      </SafeAreaView>
    );
  }

  if (meeting.isError) {
    return (
      <SafeAreaView style={styles.container} edges={['bottom']}>
        <View style={styles.content}>
          <Alert message={getErrorMessage(meeting.error)} />
        </View>
      </SafeAreaView>
    );
  }

  if (!meeting.data) {
    return null;
  }

  // A draft or merely validated PV stays internal to the syndic until published.
  const publishedMinutes = minutes.data?.status === 'PUBLISHED' ? minutes.data : null;

  return (
    <SafeAreaView style={styles.container} edges={['bottom']}>
      <ScrollView contentContainerStyle={styles.content}>
        <Card>
          <View style={styles.titleRow}>
            <Text style={styles.title}>{meeting.data.title}</Text>
            <Badge color={MEETING_STATUS_BADGE_COLORS[meeting.data.status]}>
              {MEETING_STATUS_LABELS[meeting.data.status]}
            </Badge>
          </View>
          <DetailRow label="Copropriété" value={meeting.data.propertyName} />
          <DetailRow label="Nature" value={MEETING_TYPE_LABELS[meeting.data.meetingType]} />
          <DetailRow label="Date et heure" value={formatScheduledAt(meeting.data.scheduledAt)} />
          <DetailRow label="Lieu" value={formatVenue(meeting.data)} />
        </Card>

        {meeting.data.comment ? (
          <Card>
            <Text style={styles.sectionTitle}>Note du syndic</Text>
            <RichTextContent html={meeting.data.comment} />
          </Card>
        ) : null}

        <Card>
          <Text style={styles.sectionTitle}>Ordre du jour</Text>
          {agenda.isLoading && <Loader label="Chargement de l'ordre du jour…" />}
          {agenda.isError && <Alert message={getErrorMessage(agenda.error)} />}
          {(agenda.data ?? []).map((item, index) => (
            <View key={item.id} style={styles.agendaItem}>
              <Text style={styles.agendaLabel}>
                {index + 1}. {item.label}
              </Text>
              {item.description ? <Text style={styles.agendaDescription}>{item.description}</Text> : null}
              <Text style={styles.agendaRule}>{MAJORITY_RULE_LABELS[item.majorityRule]}</Text>
              <AgendaItemResultSummary agendaItemId={item.id} enabled={item.voteSessionStatus === 'CLOSED'} />
            </View>
          ))}
        </Card>

        <Card>
          <Text style={styles.sectionTitle}>Procès-verbal</Text>
          {minutes.isError && !isNotFound(minutes.error) && <Alert message={getErrorMessage(minutes.error)} />}
          {publishedMinutes ? (
            <RichTextContent html={publishedMinutes.content} />
          ) : (
            <Text style={styles.placeholder}>
              Le procès-verbal sera consultable ici dès sa publication par le syndic.
            </Text>
          )}
        </Card>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.gray[50] },
  content: { padding: 16, gap: 12 },
  titleRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: 8, marginBottom: 8 },
  title: { flexShrink: 1, fontSize: 16, fontWeight: '600', color: colors.gray[900] },
  sectionTitle: { fontSize: 14, fontWeight: '600', color: colors.gray[900], marginBottom: 8 },
  detailRow: { flexDirection: 'row', justifyContent: 'space-between', gap: 12, paddingVertical: 4 },
  detailLabel: { fontSize: 13, color: colors.gray[500] },
  detailValue: { flexShrink: 1, fontSize: 13, color: colors.gray[900], textAlign: 'right' },
  agendaItem: { paddingVertical: 8, borderTopWidth: 1, borderTopColor: colors.gray[100] },
  agendaLabel: { fontSize: 14, fontWeight: '500', color: colors.gray[900] },
  agendaDescription: { marginTop: 2, fontSize: 13, color: colors.gray[500] },
  agendaRule: { marginTop: 2, fontSize: 12, color: colors.gray[400] },
  placeholder: { fontSize: 13, color: colors.gray[500] },
});
