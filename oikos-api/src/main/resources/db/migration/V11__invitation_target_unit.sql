-- Une invitation privée désigne désormais un lot : le syndic invite un contact
-- précis, depuis sa fiche, pour le lot qu'il lui a rattaché. Nul pour un lien
-- public (il circule, chacun y choisit son lot) et pour une invitation au
-- bureau (un siège, pas un lot) - d'où la colonne nullable plutôt qu'un
-- NOT NULL que les invitations existantes ne pourraient pas honorer.
--
-- ON DELETE SET NULL : supprimer un lot ne doit pas effacer l'invitation ni son
-- historique. L'invitation retombe simplement sur le choix libre du lot, ce que
-- la page d'accueil sait déjà afficher.
ALTER TABLE public.invitation
    ADD COLUMN target_unit_id uuid;

ALTER TABLE public.invitation
    ADD CONSTRAINT invitation_target_unit_id_fkey FOREIGN KEY (target_unit_id)
        REFERENCES public.unit(id) ON DELETE SET NULL;
