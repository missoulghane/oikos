-- Un envoi à toute la copropriété n'est plus un canal unique par copropriété
-- mais un message de plus, avec son propre objet (voir Conversation /
-- SendBroadcastMessageService). Deux verrous du schéma s'y opposaient.

-- 1. L'unicité : un seul BROADCAST par copropriété. Le deuxième envoi échouait.
DROP INDEX IF EXISTS uk_conversation_broadcast_property;

-- 2. La contrainte de sujet, qui l'interdisait justement aux BROADCAST. On la
--    remplace : GROUP et BOARD_PRIVATE en exigent toujours un, BROADCAST le
--    porte désormais - et reste tolérant pour les lignes déjà en base.
ALTER TABLE conversation DROP CONSTRAINT IF EXISTS chk_conversation_subject;
ALTER TABLE conversation ADD CONSTRAINT chk_conversation_subject
    CHECK (type = 'BROADCAST' OR subject IS NOT NULL);

-- Les envois déjà en base ont été écrits sans objet, du temps du canal unique.
-- On leur donne celui que l'interface affichait pour eux, plutôt que de laisser
-- un titre vide dans la liste.
UPDATE conversation
SET subject = 'Annonces de la copropriété'
WHERE type = 'BROADCAST'
  AND subject IS NULL;
