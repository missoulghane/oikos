import { useOutletContext } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import type { Property } from '@/features/property-mngt/properties/types/property.types';
import { useQuorumSettings } from '@/features/property-mngt/general-meetings/hooks/useQuorumSettings';
import { useSetQuorumSetting } from '@/features/property-mngt/general-meetings/hooks/useMeetingMutations';
import {
  quorumSettingSchema,
  type QuorumSettingFormValues,
} from '@/features/property-mngt/general-meetings/schemas/generalMeetingSchemas';
import { MEETING_TYPE_LABELS } from '@/features/property-mngt/general-meetings/constants/generalMeetingLabels';
import type { MeetingType } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';
import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { Card } from '@/shared/components/Card/Card';
import { Input } from '@/shared/components/Input/Input';
import { Loader } from '@/shared/components/Loader/Loader';
import { Select } from '@/shared/components/Select/Select';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

const MEETING_TYPES = Object.keys(MEETING_TYPE_LABELS) as MeetingType[];

export function MeetingQuorumSettingsTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const settings = useQuorumSettings(property.id);
  const setSetting = useSetQuorumSetting(property.id);
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<QuorumSettingFormValues>({
    resolver: zodResolver(quorumSettingSchema),
    defaultValues: { meetingType: 'ORDINARY', quorumPercentage: 0 },
  });

  return (
    <div className="flex flex-col gap-6">
      <Card className="flex flex-col gap-4">
        <div>
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white/90">Quorum</h2>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Le seuil se règle par nature d'assemblée. Il est recopié sur chaque AG à sa création : le modifier
            ensuite ne change rien à une assemblée déjà créée.
          </p>
        </div>

        {settings.isLoading && <Loader label="Chargement du paramétrage…" />}
        {settings.isError && <Alert message={getErrorMessage(settings.error)} />}

        <dl className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          {MEETING_TYPES.map((type) => {
            const configured = (settings.data ?? []).find((setting) => setting.meetingType === type);
            return (
              <div key={type}>
                <dt className="text-sm text-gray-500 dark:text-gray-400">
                  AG {MEETING_TYPE_LABELS[type].toLowerCase()}
                </dt>
                <dd className="text-gray-900 dark:text-white/90">
                  {/* An unconfigured type reads differently from an explicit zero: only the
                      second is a decision. */}
                  {configured
                    ? `${configured.quorumPercentage.toLocaleString('fr-FR')} %`
                    : 'Non paramétré (aucun quorum exigé)'}
                </dd>
              </div>
            );
          })}
        </dl>
      </Card>

      <Card className="flex flex-col gap-4">
        <h3 className="font-medium text-gray-900 dark:text-white/90">Définir un seuil</h3>
        <form
          onSubmit={handleSubmit((values) => setSetting.mutate(values))}
          className="flex flex-col gap-4 sm:max-w-md"
        >
          <Select label="Nature de l'assemblée" {...register('meetingType')} errorMessage={errors.meetingType?.message}>
            {MEETING_TYPES.map((type) => (
              <option key={type} value={type}>
                {MEETING_TYPE_LABELS[type]}
              </option>
            ))}
          </Select>
          <Input
            label="Quorum (%)"
            type="number"
            step="0.01"
            min="0"
            max="100"
            {...register('quorumPercentage', { valueAsNumber: true })}
            errorMessage={errors.quorumPercentage?.message}
          />
          {setSetting.isError && <Alert message={getErrorMessage(setSetting.error)} />}
          {setSetting.isSuccess && <Alert variant="success" message="Seuil enregistré." />}
          <Button type="submit" isLoading={setSetting.isPending}>
            Enregistrer
          </Button>
        </form>
      </Card>
    </div>
  );
}
