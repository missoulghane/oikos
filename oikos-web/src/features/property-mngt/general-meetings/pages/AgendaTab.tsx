import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { canWriteDocuments, useCurrentUser } from '@/features/identity/me';
import { AttachmentsPanel } from '@/features/property-mngt/documents';
import type { GeneralMeetingContext } from '@/features/property-mngt/general-meetings/pages/GeneralMeetingDetailLayout';
import { useAgendaItems } from '@/features/property-mngt/general-meetings/hooks/useAgendaItems';
import {
  useAddAgendaItem,
  useDeleteAgendaItem,
  useReorderAgendaItems,
  useUpdateAgendaItem,
} from '@/features/property-mngt/general-meetings/hooks/useMeetingMutations';
import { AgendaItemForm } from '@/features/property-mngt/general-meetings/components/AgendaItemForm';
import { MAJORITY_RULE_SHORT_LABELS } from '@/features/property-mngt/general-meetings/constants/generalMeetingLabels';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { Button } from '@/shared/components/Button/Button';
import { Card } from '@/shared/components/Card/Card';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Loader } from '@/shared/components/Loader/Loader';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

export function AgendaTab() {
  const { property, meeting } = useOutletContext<GeneralMeetingContext>();
  const currentUser = useCurrentUser();
  const canWriteAttachments = currentUser.data ? canWriteDocuments(currentUser.data, property.id) : false;
  const items = useAgendaItems(meeting.id);
  const addItem = useAddAgendaItem(meeting.id);
  const updateItem = useUpdateAgendaItem(meeting.id);
  const deleteItem = useDeleteAgendaItem(meeting.id);
  const reorder = useReorderAgendaItems(meeting.id);
  // The id being edited, not the item itself: the list refetches after every
  // change, and holding a stale copy would put yesterday's wording back in the
  // form. Edited in place rather than in a dialog - same inline pattern as the
  // lot's tantièmes on UnitDetailPage.
  const [editingId, setEditingId] = useState<string | null>(null);
  // Attachments are collapsed by default and opened one point at a time: an agenda of
  // fifteen points, each with its own upload field unfolded, is a wall rather than a list.
  const [openAttachmentsId, setOpenAttachmentsId] = useState<string | null>(null);

  // The agenda is never frozen for the time being (ADR 0002 §8): the strict rule
  // - an owner is convoked on the strength of a fixed list - is deferred, so a
  // point stays addable and editable at every status.
  const agenda = items.data ?? [];

  function move(index: number, direction: -1 | 1) {
    const target = index + direction;
    if (target < 0 || target >= agenda.length) {
      return;
    }
    const ids = agenda.map((item) => item.id);
    [ids[index], ids[target]] = [ids[target], ids[index]];
    reorder.mutate(ids);
  }

  return (
    <div className="flex flex-col gap-6">
      <Card className="flex flex-col gap-4">
        <h3 className="font-medium text-gray-900 dark:text-white/90">Points à l'ordre du jour</h3>

        {items.isLoading && <Loader label="Chargement de l'ordre du jour…" />}
        {items.isError && <Alert message={getErrorMessage(items.error)} />}
        {reorder.isError && <Alert message={getErrorMessage(reorder.error)} />}
        {deleteItem.isError && <Alert message={getErrorMessage(deleteItem.error)} />}

        {items.data && agenda.length === 0 && (
          <EmptyState title="Aucun point inscrit">
            Une assemblée ne peut pas être convoquée tant qu'elle n'a aucun point à décider.
          </EmptyState>
        )}

        <ol className="flex flex-col gap-3">
          {agenda.map((item, index) => (
            <li key={item.id} className="rounded-xl border border-gray-200 dark:border-gray-800 p-4">
              {editingId === item.id ? (
                <AgendaItemForm
                  item={item}
                  isSubmitting={updateItem.isPending}
                  onCancel={() => setEditingId(null)}
                  onSubmit={(values) =>
                    updateItem.mutate(
                      { agendaItemId: item.id, payload: values },
                      { onSuccess: () => setEditingId(null) },
                    )
                  }
                />
              ) : (
                <div className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
                  <div className="flex flex-col gap-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="text-sm text-gray-400 dark:text-gray-500">#{index + 1}</span>
                      <span className="font-medium text-gray-900 dark:text-white/90">{item.label}</span>
                      <Badge color="light">{MAJORITY_RULE_SHORT_LABELS[item.majorityRule]}</Badge>
                    </div>
                    {item.description && (
                      <p className="text-sm text-gray-500 dark:text-gray-400">{item.description}</p>
                    )}
                  </div>
                  <div className="flex shrink-0 flex-wrap gap-2">
                    <Button
                      variant="secondary"
                      aria-label="Monter"
                        disabled={index === 0 || reorder.isPending}
                        onClick={() => move(index, -1)}
                      >
                        ↑
                      </Button>
                      <Button
                        variant="secondary"
                        aria-label="Descendre"
                        disabled={index === agenda.length - 1 || reorder.isPending}
                        onClick={() => move(index, 1)}
                      >
                        ↓
                      </Button>
                      <Button variant="secondary" onClick={() => setEditingId(item.id)}>
                        Modifier
                      </Button>
                    <Button
                      variant="secondary"
                      onClick={() => setOpenAttachmentsId(openAttachmentsId === item.id ? null : item.id)}
                    >
                      {openAttachmentsId === item.id ? 'Masquer les pièces jointes' : 'Pièces jointes'}
                    </Button>
                    <Button
                      variant="secondary"
                      isLoading={deleteItem.isPending}
                      onClick={() => deleteItem.mutate(item.id)}
                    >
                      Supprimer
                    </Button>
                  </div>
                </div>
              )}

              {openAttachmentsId === item.id && editingId !== item.id && (
                <div className="mt-3 border-t border-gray-100 dark:border-gray-800 pt-3">
                  <p className="mb-2 text-sm text-gray-500 dark:text-gray-400">
                    Les pièces qui documentent ce point précis. Elles sont lues par les copropriétaires ; celles qui
                    concernent l'assemblée entière se joignent dans l'onglet « Informations ».
                  </p>
                  <AttachmentsPanel
                    ownerType="AGENDA_ITEM"
                    ownerId={item.id}
                    canWrite={canWriteAttachments}
                    emptyLabel="Aucune pièce jointe pour ce point."
                  />
                </div>
              )}
            </li>
          ))}
        </ol>
      </Card>

      <Card className="flex flex-col gap-4">
        <h3 className="font-medium text-gray-900 dark:text-white/90">Ajouter un point</h3>
        {addItem.isError && <Alert message={getErrorMessage(addItem.error)} />}
        <AgendaItemForm isSubmitting={addItem.isPending} onSubmit={(values) => addItem.mutate(values)} />
      </Card>
    </div>
  );
}
