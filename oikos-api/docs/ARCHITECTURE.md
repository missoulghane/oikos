# Architecture

Oikos API suit une architecture hexagonale (ports & adapters) par contexte
métier (`auth`, `user`, `shared`). Chaque contexte est découpé en quatre
couches, sous `com.architek.oikos.<contexte>` :

- `domain` — entités, value objects, exceptions et interfaces de repository
  métier. Aucune dépendance vers un framework.
- `application` — cas d'usage, commands/queries, ports d'entrée/sortie.
  Orchestre le domaine.
- `infrastructure` — implémentations techniques (persistence JPA, sécurité,
  email, configuration Spring) des ports définis par `application`/`domain`.
- `web` — contrôleurs REST, requêtes/réponses HTTP.

## Règle 1 — Direction des dépendances

Cette règle est appliquée automatiquement par deux tests :
`DependencyRulesArchTest` (analyse bytecode via ArchUnit) et
`LombokUsageSourceTest` (analyse du code source, nécessaire car Lombok est en
rétention `SOURCE` et n'est plus visible dans le bytecode compilé).

1. **`domain` ne dépend que du JDK et d'autre code `domain`.**
   Interdits dans `domain` : Spring (`org.springframework..`),
   JPA (`jakarta.persistence..`), Jackson (`com.fasterxml.jackson..`,
   `tools.jackson..`) et Lombok (`lombok..`).

2. **Le flux de dépendances est `web -> application -> domain <- infrastructure`.**
   Ni `domain` ni `application` ne dépendent de `web` ou `infrastructure`.

3. **Lombok est interdit dans `domain` et `application`.**
   Ces couches doivent rester en Java « pur » (constructeurs, getters,
   `equals`/`hashCode` écrits à la main ou via `record`), pour que leur
   comportement soit visible sans dépendre d'un traitement à la compilation.
   Lombok reste autorisé dans `infrastructure` et `web` (ex. entités JPA,
   DTOs techniques).

Toute évolution de ces règles doit rester synchronisée avec
`DependencyRulesArchTest` et `LombokUsageSourceTest` dans
`src/test/java/com/architek/oikos/architecture/`.

## Règle 2 — Nomenclature en anglais

Tout le code généré (packages, classes, méthodes, champs, valeurs d'enum,
tables/colonnes SQL, chemins d'API REST) est nommé **en anglais**, même
lorsque la spécification source (SFD, échanges avec le métier) est en
français. Le français reste la langue des specs et des échanges
fonctionnels ; l'anglais est la langue du code, sans exception.

- Traduire le vocabulaire métier au moment de la conception, avant
  d'écrire le moindre fichier — pas de renommage a posteriori.
- Voir [NOMENCLATURE.md](NOMENCLATURE.md) pour la table de correspondance
  FR → EN déjà actée (issue du renommage du module copropriété/property).
  Tout nouveau terme métier rencontré dans une future SFD doit y être
  ajouté avant implémentation, pour rester cohérent avec l'existant
  (ex. ne pas introduire `Owner` dans un module si `UnitOwnership` désigne
  déjà ce concept ailleurs).
- Les commentaires/Javadoc suivent la même règle : rédigés en anglais.
