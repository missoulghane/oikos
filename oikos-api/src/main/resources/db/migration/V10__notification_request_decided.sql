-- REQUEST_DECIDED : le pendant de REQUEST_RECEIVED, émis une fois qu'un membre
-- du syndic a tranché une demande d'adhésion - vers le demandeur (accès accordé
-- ou refusé) et vers le reste du bureau (pour qu'une demande déjà traitée ne
-- soit pas réexaminée). La contrainte de la baseline énumère les types un à un :
-- elle doit être reposée, sinon toute insertion du nouveau type est rejetée.
ALTER TABLE public.notification DROP CONSTRAINT notification_type_check;

ALTER TABLE public.notification
    ADD CONSTRAINT notification_type_check CHECK (((type)::text = ANY (ARRAY[
        ('INSTALLMENT_OVERDUE'::character varying)::text,
        ('GENERAL_MEETING_CALLED'::character varying)::text,
        ('RELAUNCH_TO_VALIDATE'::character varying)::text,
        ('REQUEST_RECEIVED'::character varying)::text,
        ('REQUEST_DECIDED'::character varying)::text,
        ('GENERAL'::character varying)::text])));
