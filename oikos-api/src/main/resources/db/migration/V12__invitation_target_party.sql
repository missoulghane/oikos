-- Une invitation privée part de la fiche d'un contact : elle lui est adressée,
-- pas seulement à son adresse email du jour. C'est ce rattachement qui permet
-- de retrouver le bon contact quand l'invité crée son compte avec une autre
-- adresse, et à la fiche de savoir qu'une invitation court encore
-- (« Invitation en cours »).
--
-- Nullable : un lien public ne s'adresse à personne, et une invitation au
-- conseil syndical vise un siège avant de viser un contact.
--
-- ON DELETE CASCADE, à la différence de target_unit_id : supprimer un contact
-- supprime la raison d'être de l'invitation qui lui était adressée, alors que
-- supprimer un lot laisse une invitation qui retombe sur le choix libre.
ALTER TABLE public.invitation
    ADD COLUMN target_party_id uuid;

ALTER TABLE public.invitation
    ADD CONSTRAINT invitation_target_party_id_fkey FOREIGN KEY (target_party_id)
        REFERENCES public.party(id) ON DELETE CASCADE;

-- Le lookup « une invitation court-elle pour ce contact ? » est fait à chaque
-- ouverture de fiche contact.
CREATE INDEX idx_invitation_target_party ON public.invitation USING btree (target_party_id)
    WHERE (target_party_id IS NOT NULL);
