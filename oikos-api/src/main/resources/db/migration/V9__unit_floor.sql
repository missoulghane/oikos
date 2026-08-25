-- Un lot se situe à un étage : l'immeuble déclare déjà son nombre d'étages
-- (building.floor_count), le lot ne disait pas lequel il occupait. La saisie
-- reste facultative - « étage inconnu » est l'état de tous les lots déjà en
-- base, et rien ne permet de le deviner rétroactivement.

ALTER TABLE public.unit ADD COLUMN floor integer;

-- Aucune borne haute en base : la cohérence avec building.floor_count est une
-- règle métier (voir AddUnitService), pas une contrainte de colonne - le
-- nombre d'étages de l'immeuble peut être corrigé à la baisse après coup, et
-- un CHECK ferait alors échouer la mise à jour de l'immeuble plutôt que de
-- signaler les lots concernés.
ALTER TABLE public.unit ADD CONSTRAINT chk_unit_floor_non_negative CHECK (floor IS NULL OR floor >= 0);
