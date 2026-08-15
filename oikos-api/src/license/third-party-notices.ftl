<#-- Markdown template for license-maven-plugin's add-third-party goal.
     Derived from the plugin's own third-party-file-groupByLicense.ftl.
     The prose below is part of the template on purpose: regenerating the file
     must not drop the pre-production checklist. -->
<#function artifactFormat artifact>
    <#local coords = artifact.groupId + ":" + artifact.artifactId + ":" + artifact.version>
    <#if artifact.name?index_of('Unnamed') &gt; -1>
        <#return "`" + coords + "`">
    <#else>
        <#return artifact.name + " — `" + coords + "`">
    </#if>
</#function>
<#function licensesKey licenses>
    <#local result = "">
    <#list licenses?sort as license>
        <#local result = result + ", " + license>
    </#list>
    <#return result?substring(2)>
</#function>
<#function aggregateLicenses dependencies>
    <#assign aggregate = {}>
    <#list dependencies as entry>
        <#assign project = artifactFormat(entry.getKey())/>
        <#assign key = licensesKey(entry.getValue())/>
        <#if aggregate[key]?? >
            <#assign aggregate = aggregate + {key : aggregate[key] + [project]} />
        <#else>
            <#assign aggregate = aggregate + {key : [project]} />
        </#if>
    </#list>
    <#return aggregate>
</#function>
# Notices tierces — oikos-api

Dépendances tierces embarquées dans le backend Java et leurs licences.

Fichier **généré** : ne le modifiez pas à la main, éditez
`src/license/third-party-notices.ftl` puis régénérez :

```bash
./mvnw license:add-third-party
```

La génération n'est **pas** branchée sur le cycle de build. C'est délibéré : le
`Dockerfile` construit hors ligne après `dependency:go-offline`, et ce goal
résout les métadonnées de licence sur le réseau — l'y attacher casserait la
construction de l'image.

Périmètre : backend Java uniquement. Les dépendances npm de `oikos-web` et
`oikos-mobile` ne sont pas couvertes ici.

## Actions à valider avant passage en production

- [ ] **Vérifier la visibilité du package `ghcr.io/<owner>/oikos-api`.** Toute la
      suite en dépend. Un package **privé**, tiré uniquement par votre propre
      serveur, n'est pas une distribution au sens de la LGPL 2.1 : aucune des
      obligations ci-dessous ne s'applique. Un package **public** est
      téléchargeable par n'importe qui, donc distribué, et elles s'appliquent
      toutes. À contrôler sur *GitHub → Packages → oikos-api → Package settings*.
- [ ] **Si le package est public, ou à la première livraison on-premise :**
      joindre le texte de la LGPL 2.1 et un lien vers les sources
      d'openhtmltopdf (<https://github.com/danfickle/openhtmltopdf>). Les trois
      autres obligations sont déjà tenues par la construction actuelle — voir la
      section suivante.
- [ ] **Faire valider cette lecture par un conseil juridique** si oikos doit être
      livré chez des clients plutôt qu'exploité en SaaS. Ce fichier est une
      analyse d'ingénierie, pas un avis juridique.
- [ ] **Régénérer ce fichier** après tout ajout ou montée de version de
      dépendance, et relire la liste ci-dessous à la recherche d'une licence
      copyleft réseau (AGPL) — celle-là contaminerait oikos même en SaaS.

## Dépendances sous copyleft

Trois entrées de la liste ci-dessous ne sont pas permissives. Une seule impose
une action :

| Dépendance | Licence | Conséquence |
|---|---|---|
| openhtmltopdf (core + pdfbox) | LGPL 2.1 ou ultérieure, **licence unique** | Obligations à la distribution, détaillées ci-dessous |
| Logback (classic + core) | EPL 2.0 **ou** LGPL 2.1, au choix | Aucune : on retient l'EPL 2.0 |
| H2 | EPL 1.0 **ou** MPL 2.0, au choix | Aucune : copyleft au fichier, et nous ne modifions rien |

**Aucune dépendance AGPL.** C'est la vérification à refaire à chaque montée de
version : ce copyleft-là s'appliquerait même en SaaS, sans distribution.

### openhtmltopdf

`openhtmltopdf-pdfbox` génère le PDF des reçus de paiement. Retenue plutôt
qu'iText 7, dont la licence AGPL imposerait la publication du code source
d'oikos du seul fait d'exposer le service en ligne.

La LGPL 2.1 n'a **pas de clause réseau** : ses obligations se déclenchent à la
distribution d'une copie du logiciel, pas à son exploitation sur vos serveurs.

| Obligation LGPL 2.1 | État |
|---|---|
| Ne pas modifier les sources de la bibliothèque | ✅ dépendance Maven brute |
| Permettre le remplacement par une version modifiée | ✅ le repackage Spring Boot conserve `BOOT-INF/lib/openhtmltopdf-pdfbox-*.jar` comme fichier distinct et remplaçable |
| Fournir les sources de la bibliothèque, ou un lien | ✅ Maven Central et GitHub |
| Mentionner son usage et joindre le texte de la licence | ⚠️ ce fichier couvre la mention ; le texte de la licence reste à joindre en cas de distribution |

Deux règles à ne jamais casser, sous peine de perdre la deuxième ligne du
tableau :

1. **Ne jamais patcher openhtmltopdf dans le dépôt.** Un correctif local devrait
   être republié sous LGPL. Sous-classez, ou remontez le correctif en amont.
2. **Ne jamais activer `maven-shade-plugin`** sur ce module : un uber-jar avec
   relocation rend le relinking impossible.

## Dépendances par licence

<#if dependencyMap?size == 0>
Aucune dépendance.
<#else>
<#assign aggregate = aggregateLicenses(dependencyMap)>
<#list aggregate?keys?sort as licenses>

### ${licenses}

<#list aggregate[licenses]?sort as project>
- ${project}
</#list>
</#list>
</#if>
