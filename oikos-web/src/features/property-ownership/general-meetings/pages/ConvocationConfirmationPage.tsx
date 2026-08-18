import { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { Card } from '@/shared/components/Card/Card';
import { Input } from '@/shared/components/Input/Input';
import { Loader } from '@/shared/components/Loader/Loader';
import {
  useConfirmConvocation,
  useConfirmConvocationByCode,
  useConvocationConfirmation,
  useConvocationConfirmationByCode,
} from '@/features/property-ownership/general-meetings/hooks/useConvocationConfirmation';
import {
  ATTENDANCE_REPLY_LABELS,
  MEETING_TYPE_LABELS,
} from '@/features/property-mngt/general-meetings/constants/generalMeetingLabels';
import type { MeetingType } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';
import { formatLotLabel } from '@/features/property-mngt/general-meetings/utils/formatMeeting';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

const DATE_TIME = new Intl.DateTimeFormat('fr-FR', {
  dateStyle: 'full',
  timeStyle: 'short',
  timeZone: 'Africa/Casablanca',
});

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex flex-col gap-0.5 sm:flex-row sm:gap-2">
      <span className="text-sm text-gray-500 dark:text-gray-400 sm:w-32 sm:shrink-0">{label}</span>
      <span className="text-gray-900 dark:text-white/90">{value}</span>
    </div>
  );
}

/**
 * The page behind the confirmation link printed on every convocation.
 *
 * <p>Public, and it has to stay that way: it exists for the copropriétaires who
 * have no account and never will. Before it, they received a convocation and
 * could only answer by telephoning the syndic - which is why every reply in the
 * system was recorded as taken at the office.
 *
 * <p>No login, no account creation, no invitation to make one. The whole page is
 * one question and two buttons; anything else on it would be a step between a
 * copropriétaire and the only thing they came to do.
 */
export function ConvocationConfirmationPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  // The two codes off the letter, for whoever arrives here without a link. Submitted
  // explicitly rather than as you type: a wrong pair counts against the server's attempt
  // cap, and firing one request per keystroke would burn through it on the way to a
  // correct code.
  const [referenceInput, setReferenceInput] = useState(searchParams.get('ag') ?? '');
  const [codeInput, setCodeInput] = useState(searchParams.get('code') ?? '');
  const [submitted, setSubmitted] = useState<{ reference: string; code: string } | null>(
    searchParams.get('ag') && searchParams.get('code')
      ? { reference: searchParams.get('ag')!, code: searchParams.get('code')! }
      : null,
  );

  // The lot code, asked for again at the moment of answering when the visitor arrived by
  // link. The link says the convocation was received, never by whom: it is forwarded,
  // printed, left on a table. The code is on the letter, in the hands of whoever answers
  // for the lot. On the paper path it was already typed to get here, so it is not asked
  // twice.
  const [lotCode, setLotCode] = useState('');

  const byToken = useConvocationConfirmation(token);
  const byCode = useConvocationConfirmationByCode(submitted?.reference ?? '', submitted?.code ?? '', Boolean(submitted));
  const confirmByToken = useConfirmConvocation(token, lotCode.trim());
  const confirmByCode = useConfirmConvocationByCode(submitted?.reference ?? '', submitted?.code ?? '');

  const confirmation = token ? byToken : byCode;
  const confirm = token ? confirmByToken : confirmByCode;

  if (!token && !submitted) {
    return (
      <AuthLayout>
        <Card className="flex flex-col gap-4">
          <div>
            <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Confirmation de présence</h1>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Saisissez les deux codes imprimés sur votre convocation. Aucun compte n'est nécessaire.
            </p>
          </div>
          <form
            className="flex flex-col gap-3"
            onSubmit={(event) => {
              event.preventDefault();
              setSubmitted({ reference: referenceInput.trim(), code: codeInput.trim() });
            }}
          >
            <Input
              label="Référence de l'assemblée"
              name="meetingReference"
              value={referenceInput}
              placeholder="x7k2m9"
              autoCapitalize="none"
              onChange={(event) => setReferenceInput(event.target.value)}
            />
            <Input
              label="Code de votre lot"
              name="confirmationCode"
              value={codeInput}
              placeholder="w754a1"
              autoCapitalize="none"
              onChange={(event) => setCodeInput(event.target.value)}
            />
            <Button type="submit" disabled={referenceInput.trim().length !== 6 || codeInput.trim().length !== 6}>
              Continuer
            </Button>
          </form>
          <p className="text-xs text-gray-400 dark:text-gray-500">
            Vous pouvez aussi scanner le QR code de votre convocation, ou suivre le lien reçu par email.
          </p>
        </Card>
      </AuthLayout>
    );
  }

  if (confirmation.isPending) {
    return (
      <AuthLayout>
        <Loader label="Chargement de votre convocation…" />
      </AuthLayout>
    );
  }

  if (confirmation.isError || !confirmation.data) {
    return (
      <AuthLayout>
        <Card className="flex flex-col gap-3">
          <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">
            {token ? 'Lien non valide' : 'Codes non reconnus'}
          </h1>
          {/* The API says the same thing for a wrong reference and a wrong code, on purpose -
              so this page cannot say which of the two to correct either. */}
          <Alert message={getErrorMessage(confirmation.error)} />
          {!token && (
            <Button variant="secondary" onClick={() => setSubmitted(null)}>
              Ressaisir les codes
            </Button>
          )}
        </Card>
      </AuthLayout>
    );
  }

  const data = confirmation.data;
  const lotLabel = formatLotLabel(data.unitNumber, data.buildingName);
  const meetingTypeLabel = MEETING_TYPE_LABELS[data.meetingType as MeetingType] ?? data.meetingType;
  const hasAnswered = data.attendanceReply !== 'NO_REPLY';
  // Held back rather than left to fail on the server: an answer sent without the code comes
  // back a 400, and telling someone their answer was refused is worse than telling them
  // beforehand what is still missing.
  const isMissingLotCode = Boolean(token) && lotCode.trim().length !== 6;

  return (
    <AuthLayout>
      <Card className="flex flex-col gap-6">
        <div className="flex flex-col gap-1">
          <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Confirmation de présence</h1>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Indiquez si votre lot sera représenté à cette assemblée générale. Aucun compte n'est nécessaire.
          </p>
        </div>

        <div className="flex flex-col gap-2 rounded-xl border border-gray-200 dark:border-gray-800 p-4">
          <Row label="Copropriété" value={data.propertyName} />
          <Row label="Assemblée" value={`${data.meetingTitle} (${meetingTypeLabel.toLowerCase()})`} />
          <Row label="Votre lot" value={lotLabel} />
          <Row
            label="Date"
            value={data.scheduledAt ? DATE_TIME.format(new Date(data.scheduledAt)) : 'à préciser'}
          />
          <Row
            label="Lieu"
            value={
              data.venueType === 'VIDEOCONFERENCE'
                ? `Visioconférence : ${data.venueLink ?? 'lien à venir'}`
                : (data.venueAddress ?? 'à préciser')
            }
          />
        </div>

        {/* One lot, one voice, whoever holds it - the same rule the letter states, repeated
            here because it is what makes a single answer per link correct. */}
        <p className="text-sm text-gray-500 dark:text-gray-400">
          Une seule réponse est enregistrée pour ce lot, quel que soit le nombre de copropriétaires qui le
          détiennent.
        </p>

        {hasAnswered && (
          <Alert
            variant="success"
            message={`Réponse enregistrée : ${ATTENDANCE_REPLY_LABELS[data.attendanceReply].toLowerCase()}${
              data.repliedAt ? ` le ${DATE_TIME.format(new Date(data.repliedAt))}` : ''
            }.${data.stillOpen ? ' Vous pouvez encore la modifier ci-dessous.' : ''}`}
          />
        )}

        {confirm.isError && <Alert message={getErrorMessage(confirm.error)} />}

        {data.stillOpen ? (
          <div className="flex flex-col gap-4">
            {token && (
              <div className="flex flex-col gap-1 sm:max-w-xs">
                <Input
                  label="Code de votre lot"
                  name="lotConfirmationCode"
                  value={lotCode}
                  placeholder="w754a1"
                  maxLength={6}
                  autoCapitalize="none"
                  autoComplete="off"
                  onChange={(event) => setLotCode(event.target.value)}
                />
                <p className="text-xs text-gray-400 dark:text-gray-500">
                  Les six caractères imprimés sur votre convocation, à côté du QR code. Ils confirment que la
                  réponse vient bien du lot indiqué.
                </p>
              </div>
            )}
            <div className="flex flex-col gap-3 sm:flex-row">
              <Button
                className="sm:flex-1"
                isLoading={confirm.isPending}
                disabled={isMissingLotCode || confirm.isPending}
                variant={data.attendanceReply === 'ATTENDING' ? 'primary' : 'secondary'}
                onClick={() => confirm.mutate('ATTENDING')}
              >
                Je serai présent(e)
              </Button>
              <Button
                className="sm:flex-1"
                isLoading={confirm.isPending}
                disabled={isMissingLotCode || confirm.isPending}
                variant={data.attendanceReply === 'NOT_ATTENDING' ? 'primary' : 'secondary'}
                onClick={() => confirm.mutate('NOT_ATTENDING')}
              >
                Je serai absent(e)
              </Button>
            </div>
          </div>
        ) : (
          // Not an error, and not a dead end either: the visitor followed a link they were
          // given, and deserves to be told what happened rather than shown a refusal.
          <Alert
            variant="warning"
            message="Cette assemblée générale a déjà commencé : les confirmations sont closes. La présence est désormais constatée par l'émargement en séance."
          />
        )}

        <p className="text-xs text-gray-400 dark:text-gray-500">
          Ce lien vaut pour le lot indiqué ci-dessus. Ne le transmettez qu'aux personnes autorisées à répondre
          pour lui.
        </p>
      </Card>
    </AuthLayout>
  );
}
