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
APP_VERIFICATION_BASE_URL=https://<domaine>/verify-email
APP_ACCOUNT_ACTIVATION_BASE_URL=https://<domaine>/activate-account
APP_PASSWORD_RESET_BASE_URL=https://<domaine>/reset-password
APP_PARTY_INVITATION_BASE_URL=https://<domaine>/accept-invitation
APP_INVITATION_BASE_URL=https://<domaine>/invitations
APP_CORS_ALLOWED_ORIGINS=https://<domaine>
```
Voir `oikos-api/.env.example` et `.env.example` (racine) pour la liste
complète commentée.

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
  cat ~/.ssh/id_ed25519.pub | ssh deploy@<IP> "cat >> ~/.ssh/authorized_keys"
  ```
  (même piège de retour à la ligne qu'en 1.5 — vérifier avec `cat -A`.)
- Ou transférer la clé privée existante via un canal de confiance
  (gestionnaire de mots de passe, clé USB chiffrée — jamais par email/Slack
  en clair).

**Le repo.** `git clone git@github.com:<owner>/oikos.git` (ou HTTPS + token
si pas de clé SSH GitHub configurée sur cette machine — distincte de la
clé SSH du VPS ci-dessus).

**Outils de dev local**, si besoin de faire tourner le projet ici : Docker
Desktop, JDK 25 (le `mvnw` du repo télécharge Maven lui-même), Node 22.

**La clé CI (`oikos_deploy_key`)** n'a pas besoin d'exister sur cette
nouvelle machine — elle vit uniquement dans le secret GitHub `VPS_SSH_KEY`,
utilisée par les runners GitHub Actions, jamais par vous en local.

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
