-- Réouverture d'une période close. Le statut redevient OPEN et la date de
-- clôture est effacée - la période ne peut pas prétendre être encore close -
-- mais la réouverture, elle, laisse sa trace : sans ces deux colonnes, le passé
-- redeviendrait modifiable sans que rien ne l'ait jamais montré.
ALTER TABLE public.period ADD COLUMN reopened_at timestamp with time zone;
ALTER TABLE public.period ADD COLUMN reopened_by_user_id uuid;

-- Aucune reprise : les périodes existantes n'ont jamais été rouvertes, la
-- colonne à NULL dit exactement cela.
