import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Badge } from '@/shared/components/Badge/Badge';
import { Button } from '@/shared/components/Button/Button';
import { Card } from '@/shared/components/Card/Card';
import { colors } from '@/shared/theme/colors';
import {
  ATTENDANCE_REPLY_LABELS,
  MEETING_STATUS_BADGE_COLORS,
  MEETING_STATUS_LABELS,
  MEETING_TYPE_LABELS,
} from '@/features/property-ownership/general-meetings/constants/generalMeetingLabels';
import {
  formatLotLabel,
  formatScheduledAt,
  formatVenue,
} from '@/features/property-ownership/general-meetings/utils/formatMeeting';
import type {
  AttendanceReply,
  MyConvocation,
} from '@/features/property-ownership/general-meetings/types/generalMeeting.types';

interface MyConvocationCardProps {
  convocation: MyConvocation;
  isReplying: boolean;
  onPress: () => void;
  onReply: (reply: AttendanceReply) => void;
}

/**
 * One card per lot and per meeting. An owner of two lots in the same AG sees
 * two cards - which is correct: each lot carries its own voice and answers for
 * itself.
 */
export function MyConvocationCard({ convocation, isReplying, onPress, onReply }: MyConvocationCardProps) {
  // Answering closes when the session opens; before that people are free to
  // change their mind, and the last answer is the one that counts.
  const canReply = convocation.meetingStatus === 'SCHEDULED' || convocation.meetingStatus === 'CONVENED';

  return (
    <Card>
      <Pressable accessibilityRole="button" onPress={onPress} style={styles.header}>
        <View style={styles.titleRow}>
          <Text style={styles.title}>{convocation.meetingTitle}</Text>
          <Badge color={MEETING_STATUS_BADGE_COLORS[convocation.meetingStatus]}>
            {MEETING_STATUS_LABELS[convocation.meetingStatus]}
          </Badge>
        </View>
        <Text style={styles.meta}>
          {convocation.propertyName} · AG {MEETING_TYPE_LABELS[convocation.meetingType].toLowerCase()}
        </Text>
        <Text style={styles.meta}>{formatScheduledAt(convocation.scheduledAt)}</Text>
        <Text style={styles.meta}>{formatVenue(convocation)}</Text>
        <Text style={styles.lot}>
          Lot concerné : {formatLotLabel(convocation.unitNumber, convocation.buildingName)}
        </Text>
      </Pressable>

      <Text style={styles.reply}>
        Votre réponse : {ATTENDANCE_REPLY_LABELS[convocation.attendanceReply]}
        {convocation.checkedIn ? ' · émargé en séance' : ''}
      </Text>

      {canReply && (
        <View style={styles.actions}>
          <Button
            variant={convocation.attendanceReply === 'ATTENDING' ? 'primary' : 'secondary'}
            isLoading={isReplying}
            onPress={() => onReply('ATTENDING')}
            style={styles.action}
          >
            Je serai présent(e)
          </Button>
          <Button
            variant={convocation.attendanceReply === 'NOT_ATTENDING' ? 'primary' : 'secondary'}
            isLoading={isReplying}
            onPress={() => onReply('NOT_ATTENDING')}
            style={styles.action}
          >
            Je serai absent(e)
          </Button>
        </View>
      )}
    </Card>
  );
}

const styles = StyleSheet.create({
  header: { gap: 4 },
  titleRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: 8 },
  title: { flexShrink: 1, fontSize: 16, fontWeight: '600', color: colors.gray[900] },
  meta: { fontSize: 13, color: colors.gray[500] },
  lot: { fontSize: 13, color: colors.gray[400] },
  reply: { marginTop: 12, fontSize: 13, color: colors.gray[700] },
  actions: { marginTop: 12, gap: 8 },
  action: { width: '100%' },
});
