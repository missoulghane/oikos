# Déploiement — procédures

Trois procédures : installation d'un serveur de recette from scratch,
travailler depuis un autre poste, et passage en production. Le pipeline
CI/CD (`.github/workflows/deploy-staging.yml`) build les images sur GitHub
Actions et les pousse sur GHCR — **aucun serveur ne build quoi que ce
soit**, il ne fait que `docker compose pull && up -d`. Voir aussi
`README.md`, section "Docker / recette", pour le flux local.

## 1. Installation from scratch sur un nouveau serveur

Prérequis : VPS Ubuntu 24.04 fraîchement provisionné, IP connue, nom de
domaine dont l'enregistrement DNS A pointe déjà vers cette IP.

### 1.1 Connexion initiale et utilisateur non-root

```bash
ssh root@<IP>
apt update && apt upgrade -y
adduser deploy
usermod -aG sudo deploy
mkdir -p /home/deploy/.ssh
cp ~/.ssh/authorized_keys /home/deploy/.ssh/
chown -R deploy:deploy /home/deploy/.ssh
chmod 700 /home/deploy/.ssh && chmod 600 /home/deploy/.ssh/authorized_keys
```

Durcissement SSH — éditer `/etc/ssh/sshd_config` : `PermitRootLogin no`,
`PasswordAuthentication no`, puis `systemctl restart ssh`.

> ⚠️ **Piège cloud-init** : sur les images cloud Ubuntu, `sshd_config`
> inclut `/etc/ssh/sshd_config.d/*.conf` **avant** ses propres directives —
> la première valeur rencontrée gagne. Un fichier généré par cloud-init
> (souvent `50-cloud-init.conf`) y force `PasswordAuthentication yes` et
> écrase silencieusement votre édition. Vérifier après coup :
> ```bash
> grep -rn "PasswordAuthentication" /etc/ssh/sshd_config /etc/ssh/sshd_config.d/*.conf
> ```
> et corriger le fichier `.conf` fautif directement s'il dit encore `yes`.

> ⚠️ **Avant de fermer la session root**, ouvrir un second terminal et
> vérifier que `ssh deploy@<IP>` fonctionne par clé, sans mot de passe.
> Sinon la session root déjà ouverte est le seul filet de sécurité.

### 1.2 Pare-feu, swap, Docker

```bash
sudo apt install -y ufw
sudo ufw allow OpenSSH && sudo ufw allow 80/tcp && sudo ufw allow 443/tcp
sudo ufw enable

sudo fallocate -l 2G /swapfile && sudo chmod 600 /swapfile
sudo mkswap /swapfile && sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

sudo apt install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker deploy
```
Se déconnecter/reconnecter (`exit` puis `ssh deploy@<IP>`) pour que le
groupe `docker` prenne effet.

### 1.3 Dossier de déploiement

```bash
sudo mkdir -p /opt/oikos/oikos-api
sudo chown -R deploy:deploy /opt/oikos
```

> ⚠️ **Faire ceci avant le premier `docker compose up`, pas après.** Si
> Docker démarre le service `flyway` alors que son point de montage
> (`oikos-api/src/main/resources/db/migration`) n'existe pas encore sur le
> disque, le démon (root) crée l'arborescence lui-même, **appartenant à
> root** — les déploiements suivants échouent alors en écriture avec
> `Permission denied`. Si ça arrive quand même : `sudo chown -R
> deploy:deploy /opt/oikos` corrige tout rétroactivement.

### 1.4 `.env` applicatifs

Jamais touchés par le pipeline CI — créés une fois ici, à la main.

```bash
nano /opt/oikos/.env
```
```env
POSTGRES_DB=oikos
POSTGRES_USER=oikos
POSTGRES_PASSWORD=<openssl rand -base64 24>
VITE_API_URL=https://<domaine>/api/v1
```

```bash
nano /opt/oikos/oikos-api/.env
```
```env
JWT_SECRET=<openssl rand -base64 32>
MAIL_USERNAME=<compte Gmail applicatif>
MAIL_PASSWORD=<mot de passe d'application Gmail>
# Origine publique d'oikos-web. TOUS les liens envoyés aux utilisateurs en
# dérivent : emails (vérification, activation, invitation, mot de passe
# oublié) et lien de confirmation des convocations, y compris son QR code.
# L'oublier ne casse rien au démarrage - les destinataires reçoivent
# simplement des liens vers localhost.
APP_PUBLIC_BASE_URL=https://<domaine>
APP_CORS_ALLOWED_ORIGINS=https://<domaine>
```
Voir `oikos-api/.env.example` et `.env.example` (racine) pour la liste
complète commentée.

> **Mise à jour d'un déploiement antérieur à `APP_PUBLIC_BASE_URL`** : les
> anciennes variables par lien (`APP_VERIFICATION_BASE_URL`,
> `APP_INVITATION_BASE_URL`, …) restent prioritaires si elles sont présentes,
> donc rien ne casse. Il suffit d'**ajouter** `APP_PUBLIC_BASE_URL` puis de
> redémarrer l'API : c'est elle qui alimente les liens ajoutés depuis, à
> commencer par la confirmation des convocations.
>
> ```bash
> cd /opt/oikos && docker compose -f docker-compose.yml -f docker-compose.staging.yml up -d api
> ```

### 1.5 Clé SSH dédiée CI + authentification GHCR

```bash
ssh-keygen -t ed25519 -f oikos_deploy_key -C "github-actions-deploy" -N ""
```

> ⚠️ **Ne jamais accepter le nom de fichier par défaut** (`id_ed25519`)
> pour cette clé — si votre machine a déjà une clé perso à cet emplacement,
> `ssh-keygen` propose de l'écraser, et une réponse distraite y perd
> l'accès admin perso. Toujours `-f` avec un nom explicite.

```bash
cat oikos_deploy_key.pub | ssh deploy@<IP> "cat >> ~/.ssh/authorized_keys"
```

> ⚠️ **`>>` sur un fichier sans retour à la ligne final concatène tout sur
> une seule ligne invalide.** Vérifier après coup :
> ```bash
> ssh deploy@<IP> "cat -A ~/.ssh/authorized_keys"
> ```
> Chaque clé doit se terminer par `$` sur sa propre ligne. Si fusionné,
> réécrire proprement depuis les fichiers locaux :
> ```bash
> cat ~/.ssh/id_ed25519.pub ~/.ssh/oikos_deploy_key.pub | ssh deploy@<IP> "cat > ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys"
> ```

Tester : `ssh -i oikos_deploy_key deploy@<IP> "echo OK"`.

GHCR : créer un PAT GitHub classique (scope `read:packages` uniquement),
puis sur le VPS :
```bash
echo <PAT> | docker login ghcr.io -u <compte GitHub> --password-stdin
```

### 1.6 Secrets GitHub Actions

`github.com/<owner>/oikos` → Settings → Secrets and variables → Actions :

| Secret | Valeur |
|---|---|
| `VPS_HOST` | IP du serveur |
| `VPS_USER` | `deploy` |
| `VPS_SSH_KEY` | contenu de la clé **privée** `oikos_deploy_key` |

### 1.7 Premier déploiement et vérification

Si le domaine diffère de `oikos-staging.tech`, adapter `Caddyfile` (racine
du repo) et le `build-args: VITE_API_URL=...` dans le workflow avant de
pousser.

Un push sur `main` déclenche le pipeline (test → build/push GHCR →
déploiement SSH). Vérifier :
```bash
ssh deploy@<IP> "cd /opt/oikos && docker compose -f docker-compose.yml -f docker-compose.staging.yml ps"
```
Tous les services `Up`/`healthy`, `flyway` `Exited (0)`. Puis :
```bash
curl -I https://<domaine>
curl -I https://<domaine>/swagger-ui.html
```

### 1.8 Inspecter et réparer les migrations

Le service `flyway` porte sa connexion dans son environnement (voir
`docker-compose.yml`) : toute sous-commande se lance donc seule, sans avoir à
retaper l'URL ni les identifiants.

```bash
docker compose run --rm flyway info      # état de chaque migration
docker compose run --rm flyway repair    # réaligne l'historique
docker compose run --rm flyway migrate   # applique ce qui est en attente
```

`info` liste chaque version avec son état : `Success`, `Pending`, ou `Failed`.
C'est ce tableau qui distingue les trois pannes courantes.

**`Migration checksum mismatch`** — un fichier déjà appliqué a été modifié
depuis. Flyway compare l'empreinte du fichier à celle enregistrée le jour de
l'application. `repair` réécrit l'empreinte dans l'historique sans rejouer la
migration ; c'est le bon geste quand la modification était cosmétique (un
commentaire, une reformulation). Si elle changeait le SQL, il faut au
contraire écrire une nouvelle migration : la base, elle, ne rejouera jamais
l'ancienne.

**`Detected applied migration not resolved locally`** — l'historique porte
une version dont le fichier n'existe plus, typiquement une base antérieure au
squash des migrations. `repair` la retire de l'historique.

**Une migration en `Failed`** — elle s'est interrompue en cours. `repair`
efface la ligne d'échec ; il reste à vérifier à la main ce qu'elle avait déjà
écrit avant de relancer `migrate`.

**Une base antérieure à un squash de migrations** — le cas le plus déroutant,
parce que `info` affiche « Success » partout. Les migrations ont été
refondues en un seul `V1__baseline.sql` à deux reprises (commits `e11c281` et
`bfb0226`) : une base migrée avant garde dans son historique des versions
2, 3, … dont les fichiers n'existent plus, et dont les numéros sont désormais
repris par d'autres migrations. Celles du dépôt ne s'appliqueront donc
jamais — Flyway les croit déjà passées.

Le signe qui ne trompe pas : la colonne `Description` de `info` ne
correspond pas au nom des fichiers présents dans `db/migration/`.

- **En local** : recréer le volume, c'est la seule issue raisonnable.
  `docker compose down -v && docker compose up -d` — les données de dev sont
  reséedées depuis `db/dev/dev.sql`. Attention, `-v` détruit aussi
  `api-storage` (les documents téléversés en local).
- **Sur un environnement dont les données comptent** : ne rien détruire.
  Comparer d'abord le schéma réel à celui que produit `V1__baseline.sql`,
  puis renuméroter les migrations en attente au-dessus du dernier numéro
  déjà consommé par l'historique. C'est une décision à prendre au cas par
  cas, pas une commande à recopier.

> Ne pas passer `-url`/`-user` à la main dans ces commandes : ces valeurs
> viennent du `.env` racine, que Compose lit, mais que le shell ne connaît
> pas. Elles arriveraient vides, et Flyway se plaindrait d'un « user name
> not specified » qui ressemble à tort à un problème de base de données.

## 2. Travailler depuis un autre ordinateur (même compte GitHub)

Rien à refaire côté GitHub : repo, Actions, Secrets sont attachés au
compte, pas à la machine — accessibles depuis n'importe quel navigateur
connecté.

Ce qui est **local à chaque machine** et à reconfigurer :

**Accès SSH au VPS.** La nouvelle machine n'a pas la clé privée perso qui
autorise `deploy@<IP>`. Deux options :
- Générer une **nouvelle** paire de clés sur cette machine et l'ajouter en
  plus dans `authorized_keys` du VPS (recommandé : révocable
  indépendamment si cette machine est perdue, sans toucher à l'accès de
  la première) :
  ```bash
  ssh-keygen -t ed25519 -C "morad-<nom-machine>"
  ```
  Puis autoriser cette clé sur le VPS. ⚠️ **La commande ci-dessous doit être
  lancée depuis une machine qui a *déjà* un accès autorisé** (la première),
  pas depuis la nouvelle — sinon c'est l'œuf et la poule : le VPS n'accepte
  que `publickey`, donc la nouvelle machine ne peut pas s'y connecter pour
  y déposer sa propre clé. Copier le contenu de `id_ed25519.pub` de la
  nouvelle machine, puis, **depuis l'ancienne** :
  ```bash
  echo 'ssh-ed25519 AAAA... morad-<nom-machine>' \
    | ssh deploy@<IP> "cat >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys"
  ```
  (même piège de retour à la ligne qu'en 1.5 — vérifier avec `cat -A` :
  chaque clé doit occuper sa propre ligne.)

  Si **aucune** machine n'a plus d'accès (VPS réinstallé, `authorized_keys`
  perdu), le seul recours est la **console web/VNC de l'hébergeur** : s'y
  connecter en root et créer l'entrée à la main, comme en 1.1.
- Ou transférer la clé privée existante via un canal de confiance
  (gestionnaire de mots de passe, clé USB chiffrée — jamais par email/Slack
  en clair).

> ⚠️ **`REMOTE HOST IDENTIFICATION HAS CHANGED!`** — après une
> réinstallation du VPS, les clés d'hôte sont régénérées et SSH refuse de se
> connecter tant que l'ancienne entrée traîne dans `known_hosts`. Ce n'est
> pas une attaque *si* le serveur a bien été reconstruit. Vérifier
> l'empreinte **hors-bande** (console de l'hébergeur, pas par SSH) :
> ```bash
> ssh-keygen -lf /etc/ssh/ssh_host_ed25519_key.pub
> ```
> puis, si elle correspond à celle affichée dans l'avertissement, purger
> l'entrée périmée (une sauvegarde `known_hosts.old` est créée
> automatiquement) et se reconnecter :
> ```bash
> ssh-keygen -f ~/.ssh/known_hosts -R '<IP>'
> ```
> À faire sur **chaque** machine qui se connecte au VPS. Si l'empreinte ne
> correspond pas — ou si plus aucune clé n'est acceptée alors que rien n'a
> été réinstallé — vérifier dans le panneau de l'hébergeur que l'IP
> appartient toujours à votre instance : une IP réattribuée donne les mêmes
> symptômes.

**Le repo.** `git clone git@github.com:<owner>/oikos.git` (ou HTTPS + token
si pas de clé SSH GitHub configurée sur cette machine — distincte de la
clé SSH du VPS ci-dessus).

**Outils de dev local**, si besoin de faire tourner le projet ici : Docker
Desktop, JDK 25 (le `mvnw` du repo télécharge Maven lui-même), Node 22.

**La clé CI (`oikos_deploy_key`)** n'a pas besoin d'exister sur cette
nouvelle machine — elle vit uniquement dans le secret GitHub `VPS_SSH_KEY`,
utilisée par les runners GitHub Actions, jamais par vous en local.

## 2 bis. Liens universels (ouvrir l'app mobile depuis un email)

Les emails du produit (vérification, activation, invitation, mot de passe
oublié) portent des URL `https` vers le web. Sur un téléphone où l'app est
installée, le système peut les ouvrir dans l'app plutôt que dans le
navigateur — à condition de trouver, sur le domaine, un fichier qui le lui
autorise.

Ces deux fichiers sont servis par `oikos-web` sous `/.well-known/` et
versionnés dans `oikos-web/public/.well-known/`. Chacun attend **une valeur
qu'il faut aller chercher** :

| Fichier | Valeur à remplir | Où la trouver |
| --- | --- | --- |
| `apple-app-site-association` | `REMPLACER_PAR_APPLE_TEAM_ID` | Apple Developer → Membership → Team ID (10 caractères) |
| `assetlinks.json` | `REMPLACER_PAR_EMPREINTE_SHA256_DU_CERTIFICAT` | `eas credentials` → Android → le SHA-256 du certificat de **signature de l'app publiée** (celui de Play App Signing, pas celui d'upload) |

Tant qu'elles ne sont pas remplies, rien ne casse : les liens s'ouvrent dans
le navigateur, exactement comme aujourd'hui.

**Vérifier après déploiement** — les deux fichiers doivent répondre en 200,
en `application/json`, sans redirection :

```bash
curl -sI https://<domaine>/.well-known/apple-app-site-association | head -3
curl -sI https://<domaine>/.well-known/assetlinks.json | head -3
```

Un `content-type: text/html` ou un corps qui commence par `<!doctype html>`
signale que le repli SPA a répondu à la place du fichier : Apple et Google
lisent alors une page web comme une association valide mais illisible, et
échouent en silence.

**Côté application**, les mêmes domaines sont déclarés dans
`oikos-mobile/app.json` (`ios.associatedDomains`, `android.intentFilters`) et
dans `RootNavigator.tsx` (`WEB_ORIGINS`). Ajouter un domaine, c'est éditer
les trois, puis reconstruire l'app : ces déclarations partent dans le binaire,
une mise à jour du serveur seule ne suffit pas.

**Ce qui n'ouvre volontairement pas l'app** : la confirmation de convocation
(`/convocations/confirmation`). Elle existe pour les copropriétaires qui n'ont
pas de compte et n'auront pas l'app ; l'ouvrir dans l'app enverrait sur un
écran de connexion exactement ceux qui n'en ont pas.

## 3. Passage en production (une fois validé en recette)

Le principe reste identique à la section 1, appliqué à un nouveau serveur,
avec ces différences :

**Isolation du pipeline.** Ne pas réutiliser `deploy-staging.yml` tel
quel : un push sur `main` ne doit pas pouvoir toucher la prod par accident.
Deux approches possibles, à trancher avant d'implémenter :
- Un second workflow (`deploy-production.yml`) déclenché différemment
  (tag `v*`, ou push sur une branche `production` dédiée), avec ses
  propres secrets (`PROD_VPS_HOST`, `PROD_VPS_USER`, `PROD_VPS_SSH_KEY`).
- Un [GitHub Environment](https://docs.github.com/actions/deployment/targeting-different-environments/using-environments-for-deployment)
  `production` avec règles de protection (reviewers requis avant que le
  job de déploiement ne s'exécute) — plus proche du modèle GitHub natif,
  recommandé si plusieurs personnes doivent valider chaque mise en prod.

**Secrets applicatifs propres à la prod**, jamais réutilisés depuis la
recette : `JWT_SECRET`, `POSTGRES_PASSWORD`, compte mail dédié (ou au
moins un expéditeur distinct), tous régénérés — voir 1.4.

**Nouveau domaine** : adapter `Caddyfile`, `VITE_API_URL`,
`APP_*_BASE_URL`, `APP_CORS_ALLOWED_ORIGINS` en conséquence (mêmes clés
que 1.4/1.7, valeurs différentes).

**`docker-compose.production.yml`**, sur le modèle de
`docker-compose.staging.yml` — dimensionner `mem_limit` selon la RAM
réelle du serveur prod plutôt que de recopier les valeurs 4 Go de la
recette.

**Bootstrap serveur** : section 1 entière, sur le nouveau serveur — les
mêmes pièges (SSH cloud-init, ownership `/opt`, retour à la ligne
`authorized_keys`) s'appliquent à l'identique.

**Avant bascule** : checklist de validation en recette (parcours
utilisateur complet, `docker compose ps` healthy, TLS valide, restauration
d'une sauvegarde Postgres testée au moins une fois) — la prod ne doit pas
être le premier endroit où un problème de ce type se découvre.
