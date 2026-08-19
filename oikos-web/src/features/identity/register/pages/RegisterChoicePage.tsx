import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { Button } from '@/shared/components/Button/Button';
import { SelectableCard } from '@/shared/components/SelectableCard/SelectableCard';

interface AccountChoice {
  value: string;
  title: string;
  description: string;
  path: string;
}

/**
 * Un seul groupe de radios pour les deux intentions : les choix s'excluent
 * mutuellement (on ne crée qu'un compte à la fois), les intitulés
 * « Gérer / Accéder » ne sont donc que des sous-titres de regroupement.
 */
const CHOICE_GROUPS: { intent: string; choices: AccountChoice[] }[] = [
  {
    intent: 'Gérer une copropriété',
    choices: [
      {
        value: 'board-admin',
        title: 'Syndic bénévole',
        description: 'Je gère moi-même ma copropriété, sans cabinet professionnel.',
        path: '/register/board-admin',
      },
      {
        value: 'manager-admin',
        title: 'Syndic professionnel',
        description: 'Je représente un cabinet de syndic et gère plusieurs copropriétés.',
        path: '/register/manager-admin',
      },
    ],
  },
  {
    intent: 'Accéder à ma copropriété',
    choices: [
      {
        value: 'user',
        title: 'Copropriétaire',
        description: 'Je consulte mes lots, mes charges et les documents de ma copropriété.',
        path: '/register/user',
      },
    ],
  },
];

const ALL_CHOICES = CHOICE_GROUPS.flatMap((group) => group.choices);

export function RegisterChoicePage() {
  const navigate = useNavigate();
  const [selected, setSelected] = useState<string | null>(null);
  const selectedChoice = ALL_CHOICES.find((choice) => choice.value === selected);

  return (
    <AuthLayout>
      <Card>
        <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Que souhaitez-vous faire ?</h1>
        <p className="mt-1 mb-6 text-sm text-gray-600 dark:text-gray-400">
          Choisissez le type de compte à créer.
        </p>

        <div className="flex flex-col gap-6">
          {CHOICE_GROUPS.map((group) => (
            <div key={group.intent} className="flex flex-col gap-3">
              <h2 className="text-xs font-medium uppercase tracking-wide text-gray-500 dark:text-gray-400">
                {group.intent}
              </h2>
              {group.choices.map((choice) => (
                <SelectableCard
                  key={choice.value}
                  name="account-type"
                  value={choice.value}
                  checked={selected === choice.value}
                  onSelect={setSelected}
                  title={choice.title}
                  description={choice.description}
                />
              ))}
            </div>
          ))}

          <Button type="button" disabled={!selectedChoice} onClick={() => navigate(selectedChoice!.path)}>
            Continuer
          </Button>
        </div>

        <p className="mt-4 text-center text-sm text-gray-600 dark:text-gray-400">
          Déjà un compte ?{' '}
          <Link to="/login" className="font-medium text-gray-900 dark:text-white/90 underline">
            Se connecter
          </Link>
        </p>
      </Card>
    </AuthLayout>
  );
}
