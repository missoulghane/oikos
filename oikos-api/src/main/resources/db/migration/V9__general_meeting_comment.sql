-- Commentaire global de l'assemblée : la note d'intention du syndic, en texte
-- riche, lue par les copropriétaires.
--
-- `text` et non `varchar(n)` : même choix que meeting_minutes.content, et pour
-- la même raison — c'est du HTML produit par un éditeur riche, dont la longueur
-- utile n'a pas de rapport avec le nombre de caractères visibles (un paragraphe
-- de trois lignes en pèse le double une fois balisé). Une borne arbitraire ne
-- protégerait de rien et couperait un texte au milieu d'une balise.
--
-- Nullable : la plupart des AG n'en auront pas. Une chaîne vide et NULL
-- diraient la même chose, et l'application n'enregistre que NULL — voir
-- UpdateGeneralMeetingCommentService, qui normalise le blanc.
--
-- Rien à migrer pour les pièces jointes de l'AG : `document.owner_type` est un
-- varchar(20) sans contrainte de valeurs, la nouvelle constante
-- DocumentOwnerType.GENERAL_MEETING y tient telle quelle.

ALTER TABLE general_meeting ADD COLUMN comment text;
