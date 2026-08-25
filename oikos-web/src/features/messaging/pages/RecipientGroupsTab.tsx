import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { isBoardTierOnProperty, isManagerTierOnProperty, useCurrentUser } from '@/features/identity/me';
import { RecipientPicker } from '@/features/messaging/components/RecipientPicker';
import {
  useCreateRecipientGroup,
  useDeleteRecipientGroup,
  useRecipientGroups,
  useUpdateRecipientGroup,
} from '@/features/messaging/hooks/useRecipientGroups';
import { Button } from '@/shared/components/Button/Button';
import { Card } from '@/shared/components/Card/Card';
import { Input } from '@/shared/components/Input/Input';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Loader } from '@/shared/components/Loader/Loader';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { RecipientCandidate, RecipientGroup } from '@/features/messaging/types/messaging.types';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

/** Les membres d'un groupe, remis dans la forme que le sélecteur manipule. */
function toCandidates(group: RecipientGroup): RecipientCandidate[] {
  return group.members.map((member) => ({
    userId: member.userId,
    fullName: member.fullName,
    roleLabel: '',
    unitNumbers: [],
    isStaff: false,
  }));
}

/**
 * Les groupes de destinataires d'une copropriété : un carnet d'adresses, tenu
 * par le bureau. Envoyer à un groupe déplie ses membres au moment de composer -
 * le message part aux personnes, et rien ici ne rattrape un message déjà parti.
 *
 * <p>L'écran est en lecture seule pour qui ne gère pas la copropriété : voir
 * les groupes qu'on peut utiliser est utile, les modifier ne l'est pas.
 */
export function RecipientGroupsTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const currentUser = useCurrentUser();
  // Bureau bénévole comme syndic professionnel : les deux tiers tiennent le
  // carnet, exactement la population de managesProperty côté API.
  const canManage = Boolean(
    currentUser.data &&
      (isBoardTierOnProperty(currentUser.data, property.id) || isManagerTierOnProperty(currentUser.data, property.id)),
  );

  const groups = useRecipientGroups(property.id);
  const createGroup = useCreateRecipientGroup(property.id);
  const updateGroup = useUpdateRecipientGroup(property.id);
  const deleteGroup = useDeleteRecipientGroup(property.id);

  const [newName, setNewName] = useState('');
  const [newMembers, setNewMembers] = useState<RecipientCandidate[]>([]);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editName, setEditName] = useState('');
  const [editMembers, setEditMembers] = useState<RecipientCandidate[]>([]);

  const saveError = createGroup.error ?? updateGroup.error ?? deleteGroup.error;

  function handleCreate() {
    createGroup.mutate(
      { name: newName.trim(), memberUserIds: newMembers.map((member) => member.userId) },
      {
        onSuccess: () => {
          setNewName('');
          setNewMembers([]);
        },
      },
    );
  }

  function startEditing(group: RecipientGroup) {
    setEditingId(group.id);
    setEditName(group.name);
    setEditMembers(toCandidates(group));
  }

  function handleUpdate(groupId: string) {
    updateGroup.mutate(
      { groupId, payload: { name: editName.trim(), memberUserIds: editMembers.map((member) => member.userId) } },
      { onSuccess: () => setEditingId(null) },
    );
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">Groupes de destinataires</h2>
        <p className="text-sm text-gray-500 dark:text-gray-400">
          Des listes nommées pour écrire à plusieurs personnes sans les resélectionner à chaque fois. Envoyer à un
          groupe crée un message adressé à ses membres : le modifier ensuite ne change rien à ce qui est déjà parti.
        </p>
      </div>

      {saveError && <Alert message={getErrorMessage(saveError)} />}

      {canManage && (
        <Card className="flex flex-col gap-4">
          <h3 className="text-sm font-semibold text-gray-900 dark:text-white/90">Nouveau groupe</h3>
          <Input
            label="Nom du groupe"
            name="recipient-group-name"
            placeholder="Habitants du bâtiment 1"
            value={newName}
            disabled={createGroup.isPending}
            onChange={(event) => setNewName(event.target.value)}
          />
          {/* showGroups coupé : on compose un groupe, il n'a pas à s'offrir lui-même comme raccourci. */}
          <RecipientPicker
            propertyId={property.id}
            value={newMembers}
            onChange={setNewMembers}
            disabled={createGroup.isPending}
            showGroups={false}
          />
          <Button
            type="button"
            className="self-start"
            isLoading={createGroup.isPending}
            disabled={newName.trim().length === 0 || newMembers.length === 0}
            onClick={handleCreate}
          >
            Créer le groupe
          </Button>
        </Card>
      )}

      {groups.isLoading && <Loader label="Chargement des groupes…" />}
      {groups.isError && <Alert message={getErrorMessage(groups.error)} />}
      {groups.data && groups.data.length === 0 && (
        <EmptyState title="Aucun groupe">
          {canManage
            ? 'Créez un premier groupe ci-dessus pour écrire à un bâtiment, un étage, une liste de votre choix.'
            : 'Le bureau n’a pas encore créé de groupe sur cette copropriété.'}
        </EmptyState>
      )}

      {groups.data && groups.data.length > 0 && (
        <ul className="flex flex-col gap-3">
          {groups.data.map((group) => (
            <li key={group.id}>
              <Card className="flex flex-col gap-3">
                {editingId === group.id ? (
                  <>
                    <Input
                      label="Nom du groupe"
                      name={`recipient-group-name-${group.id}`}
                      value={editName}
                      disabled={updateGroup.isPending}
                      onChange={(event) => setEditName(event.target.value)}
                    />
                    <RecipientPicker
                      propertyId={property.id}
                      value={editMembers}
                      onChange={setEditMembers}
                      disabled={updateGroup.isPending}
                      showGroups={false}
                    />
                    <div className="flex gap-2">
                      <Button
                        type="button"
                        isLoading={updateGroup.isPending}
                        disabled={editName.trim().length === 0 || editMembers.length === 0}
                        onClick={() => handleUpdate(group.id)}
                      >
                        Enregistrer
                      </Button>
                      <Button type="button" variant="secondary" onClick={() => setEditingId(null)}>
                        Annuler
                      </Button>
                    </div>
                  </>
                ) : (
                  <>
                    <div className="flex flex-wrap items-start justify-between gap-3">
                      <div className="min-w-0">
                        <p className="font-medium text-gray-900 dark:text-white/90">{group.name}</p>
                        <p className="text-sm text-gray-500 dark:text-gray-400">
                          {group.members.length} membre{group.members.length > 1 ? 's' : ''}
                        </p>
                      </div>
                      {canManage && (
                        <div className="flex gap-2">
                          <Button type="button" variant="secondary" onClick={() => startEditing(group)}>
                            Modifier
                          </Button>
                          <Button
                            type="button"
                            variant="secondary"
                            isLoading={deleteGroup.isPending && deleteGroup.variables === group.id}
                            onClick={() => deleteGroup.mutate(group.id)}
                          >
                            Supprimer
                          </Button>
                        </div>
                      )}
                    </div>
                    <p className="text-sm text-gray-500 dark:text-gray-400">
                      {group.members.map((member) => member.fullName).join(', ')}
                    </p>
                  </>
                )}
              </Card>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
