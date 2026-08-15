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


### Apache-2.0

- Apache Commons Lang — `org.apache.commons:commons-lang3:3.20.0`
- Apache Commons Logging — `commons-logging:commons-logging:1.3.6`
- Apache FontBox — `org.apache.pdfbox:fontbox:3.0.3`
- Apache Log4j API — `org.apache.logging.log4j:log4j-api:2.25.4`
- Apache PDFBox — `org.apache.pdfbox:pdfbox:3.0.3`
- Apache PDFBox io — `org.apache.pdfbox:pdfbox-io:3.0.3`
- Apache XmpBox — `org.apache.pdfbox:xmpbox:3.0.3`
- Log4j API to SLF4J Adapter — `org.apache.logging.log4j:log4j-to-slf4j:2.25.4`
- Swagger UI — `org.webjars:swagger-ui:5.21.0`

### Apache License, Version 2.0

- Byte Buddy (without dependencies) — `net.bytebuddy:byte-buddy:1.18.10`
- ClassMate — `com.fasterxml:classmate:1.7.3`
- flyway-core — `org.flywaydb:flyway-core:12.4.0`
- flyway-database-postgresql — `org.flywaydb:flyway-database-postgresql:12.4.0`
- Hibernate ORM - hibernate-core — `org.hibernate.orm:hibernate-core:7.4.1.Final`
- JJWT :: API — `io.jsonwebtoken:jjwt-api:0.12.6`
- JJWT :: Extensions :: Jackson — `io.jsonwebtoken:jjwt-jackson:0.12.6`
- JJWT :: Impl — `io.jsonwebtoken:jjwt-impl:0.12.6`
- PDFBox-Graphics2d — `de.rototor.pdfbox:graphics2d:3.0.1`
- SnakeYAML — `org.yaml:snakeyaml:2.6`
- Spring AOP — `org.springframework:spring-aop:7.0.8`
- Spring Aspects — `org.springframework:spring-aspects:7.0.8`
- Spring Beans — `org.springframework:spring-beans:7.0.8`
- spring-boot — `org.springframework.boot:spring-boot:4.1.0`
- spring-boot-actuator — `org.springframework.boot:spring-boot-actuator:4.1.0`
- spring-boot-actuator-autoconfigure — `org.springframework.boot:spring-boot-actuator-autoconfigure:4.1.0`
- spring-boot-autoconfigure — `org.springframework.boot:spring-boot-autoconfigure:4.1.0`
- spring-boot-data-commons — `org.springframework.boot:spring-boot-data-commons:4.1.0`
- spring-boot-data-jpa — `org.springframework.boot:spring-boot-data-jpa:4.1.0`
- spring-boot-devtools — `org.springframework.boot:spring-boot-devtools:4.1.0`
- spring-boot-flyway — `org.springframework.boot:spring-boot-flyway:4.1.0`
- spring-boot-h2console — `org.springframework.boot:spring-boot-h2console:4.1.0`
- spring-boot-health — `org.springframework.boot:spring-boot-health:4.1.0`
- spring-boot-hibernate — `org.springframework.boot:spring-boot-hibernate:4.1.0`
- spring-boot-http-converter — `org.springframework.boot:spring-boot-http-converter:4.1.0`
- spring-boot-jackson — `org.springframework.boot:spring-boot-jackson:4.1.0`
- spring-boot-jdbc — `org.springframework.boot:spring-boot-jdbc:4.1.0`
- spring-boot-jpa — `org.springframework.boot:spring-boot-jpa:4.1.0`
- spring-boot-mail — `org.springframework.boot:spring-boot-mail:4.1.0`
- spring-boot-micrometer-metrics — `org.springframework.boot:spring-boot-micrometer-metrics:4.1.0`
- spring-boot-micrometer-observation — `org.springframework.boot:spring-boot-micrometer-observation:4.1.0`
- spring-boot-persistence — `org.springframework.boot:spring-boot-persistence:4.1.0`
- spring-boot-security — `org.springframework.boot:spring-boot-security:4.1.0`
- spring-boot-servlet — `org.springframework.boot:spring-boot-servlet:4.1.0`
- spring-boot-sql — `org.springframework.boot:spring-boot-sql:4.1.0`
- spring-boot-starter — `org.springframework.boot:spring-boot-starter:4.1.0`
- spring-boot-starter-actuator — `org.springframework.boot:spring-boot-starter-actuator:4.1.0`
- spring-boot-starter-data-jpa — `org.springframework.boot:spring-boot-starter-data-jpa:4.1.0`
- spring-boot-starter-flyway — `org.springframework.boot:spring-boot-starter-flyway:4.1.0`
- spring-boot-starter-jackson — `org.springframework.boot:spring-boot-starter-jackson:4.1.0`
- spring-boot-starter-jdbc — `org.springframework.boot:spring-boot-starter-jdbc:4.1.0`
- spring-boot-starter-logging — `org.springframework.boot:spring-boot-starter-logging:4.1.0`
- spring-boot-starter-mail — `org.springframework.boot:spring-boot-starter-mail:4.1.0`
- spring-boot-starter-micrometer-metrics — `org.springframework.boot:spring-boot-starter-micrometer-metrics:4.1.0`
- spring-boot-starter-security — `org.springframework.boot:spring-boot-starter-security:4.1.0`
- spring-boot-starter-thymeleaf — `org.springframework.boot:spring-boot-starter-thymeleaf:4.1.0`
- spring-boot-starter-tomcat — `org.springframework.boot:spring-boot-starter-tomcat:4.1.0`
- spring-boot-starter-tomcat-runtime — `org.springframework.boot:spring-boot-starter-tomcat-runtime:4.1.0`
- spring-boot-starter-validation — `org.springframework.boot:spring-boot-starter-validation:4.1.0`
- spring-boot-starter-webmvc — `org.springframework.boot:spring-boot-starter-webmvc:4.1.0`
- spring-boot-thymeleaf — `org.springframework.boot:spring-boot-thymeleaf:4.1.0`
- spring-boot-tomcat — `org.springframework.boot:spring-boot-tomcat:4.1.0`
- spring-boot-transaction — `org.springframework.boot:spring-boot-transaction:4.1.0`
- spring-boot-validation — `org.springframework.boot:spring-boot-validation:4.1.0`
- spring-boot-webmvc — `org.springframework.boot:spring-boot-webmvc:4.1.0`
- spring-boot-web-server — `org.springframework.boot:spring-boot-web-server:4.1.0`
- Spring Context — `org.springframework:spring-context:7.0.8`
- Spring Context Support — `org.springframework:spring-context-support:7.0.8`
- Spring Core — `org.springframework:spring-core:7.0.8`
- Spring Data Core — `org.springframework.data:spring-data-commons:4.1.0`
- Spring Data JPA — `org.springframework.data:spring-data-jpa:4.1.0`
- Spring Expression Language (SpEL) — `org.springframework:spring-expression:7.0.8`
- Spring JDBC — `org.springframework:spring-jdbc:7.0.8`
- Spring Object/Relational Mapping — `org.springframework:spring-orm:7.0.8`
- spring-security-config — `org.springframework.security:spring-security-config:7.1.0`
- spring-security-core — `org.springframework.security:spring-security-core:7.1.0`
- spring-security-crypto — `org.springframework.security:spring-security-crypto:7.1.0`
- spring-security-web — `org.springframework.security:spring-security-web:7.1.0`
- Spring Transaction — `org.springframework:spring-tx:7.0.8`
- Spring Web — `org.springframework:spring-web:7.0.8`
- Spring Web MVC — `org.springframework:spring-webmvc:7.0.8`
- tomcat-embed-core — `org.apache.tomcat.embed:tomcat-embed-core:11.0.22`
- tomcat-embed-el — `org.apache.tomcat.embed:tomcat-embed-el:11.0.22`
- tomcat-embed-websocket — `org.apache.tomcat.embed:tomcat-embed-websocket:11.0.22`

### Apache License 2.0

- Hibernate Validator Engine — `org.hibernate.validator:hibernate-validator:9.1.0.Final`
- Jakarta Validation API — `jakarta.validation:jakarta.validation-api:3.1.1`
- JBoss Logging 3 — `org.jboss.logging:jboss-logging:3.6.3.Final`
- swagger-annotations-jakarta — `io.swagger.core.v3:swagger-annotations-jakarta:2.2.30`
- swagger-core-jakarta — `io.swagger.core.v3:swagger-core-jakarta:2.2.30`
- swagger-models-jakarta — `io.swagger.core.v3:swagger-models-jakarta:2.2.30`

### Apache License Version 2.0

- Hibernate Models — `org.hibernate.models:hibernate-models:1.1.1`

### BSD-2-Clause

- PostgreSQL JDBC Driver — `org.postgresql:postgresql:42.7.11`

### BSD-2-Clause, Public Domain, per Creative Commons CC0

- HdrHistogram — `org.hdrhistogram:HdrHistogram:2.2.2`

### BSD-3-Clause

- ANTLR 4 Runtime — `org.antlr:antlr4-runtime:4.13.2`

### Eclipse Distribution License v. 1.0, Eclipse Public License v. 2.0

- Jakarta Persistence API — `jakarta.persistence:jakarta.persistence-api:3.2.0`

### Eclipse Distribution License - v 1.0

- istack common utility code runtime — `com.sun.istack:istack-commons-runtime:4.1.2`
- Jakarta XML Binding API — `jakarta.xml.bind:jakarta.xml.bind-api:4.0.5`
- JAXB Core — `org.glassfish.jaxb:jaxb-core:4.0.9`
- JAXB Runtime — `org.glassfish.jaxb:jaxb-runtime:4.0.9`
- TXW2 Runtime — `org.glassfish.jaxb:txw2:4.0.9`

### Eclipse Public License - v 2.0

- AspectJ Weaver — `org.aspectj:aspectjweaver:1.9.25.1`

### EDL 1.0

- Angus Activation Registries — `org.eclipse.angus:angus-activation:2.0.3`
- Jakarta Activation API — `jakarta.activation:jakarta.activation-api:2.1.4`

### EDL 1.0, EPL 2.0, GPL2 w/ CPE

- Angus Mail Provider — `org.eclipse.angus:angus-mail:2.0.5`
- Jakarta Mail API — `jakarta.mail:jakarta.mail-api:2.1.5`

### EPL 1.0, MPL 2.0

- H2 Database Engine — `com.h2database:h2:2.4.240`

### EPL 2.0, GPL2 w/ CPE

- jakarta.transaction API — `jakarta.transaction:jakarta.transaction-api:2.0.1`
- Jakarta Annotations API — `jakarta.annotation:jakarta.annotation-api:3.0.0`
- Jakarta Servlet — `jakarta.servlet:jakarta.servlet-api:6.1.0`

### EPL-2.0, LGPL-2.1-only

- Logback Classic Module — `ch.qos.logback:logback-classic:1.5.34`
- Logback Core Module — `ch.qos.logback:logback-core:1.5.34`

### GNU Lesser General Public License (LGPL), version 2.1 or later

- Openhtmltopdf Core Renderer — `io.github.openhtmltopdf:openhtmltopdf-core:1.1.37`
- Openhtmltopdf PDF Rendering (Apache PDF-BOX 3) — `io.github.openhtmltopdf:openhtmltopdf-pdfbox:1.1.37`

### MIT

- JUL to SLF4J bridge — `org.slf4j:jul-to-slf4j:2.0.18`
- SLF4J API Module — `org.slf4j:slf4j-api:2.0.18`
- webjars-locator-lite — `org.webjars:webjars-locator-lite:1.1.3`

### The Apache License, Version 2.0

- JSpecify annotations — `org.jspecify:jspecify:1.0.0`
- springdoc-openapi-starter-common — `org.springdoc:springdoc-openapi-starter-common:2.8.9`
- springdoc-openapi-starter-webmvc-api — `org.springdoc:springdoc-openapi-starter-webmvc-api:2.8.9`
- springdoc-openapi-starter-webmvc-ui — `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9`

### The Apache Software License, Version 2.0

- attoparser — `org.attoparser:attoparser:2.0.7.RELEASE`
- HikariCP — `com.zaxxer:HikariCP:7.0.2`
- Jackson-annotations — `com.fasterxml.jackson.core:jackson-annotations:2.21`
- Jackson-core — `com.fasterxml.jackson.core:jackson-core:2.21.4`
- Jackson-core — `tools.jackson.core:jackson-core:3.1.4`
- jackson-databind — `com.fasterxml.jackson.core:jackson-databind:2.21.4`
- jackson-databind — `tools.jackson.core:jackson-databind:3.1.4`
- Jackson-dataformat-YAML — `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.21.4`
- Jackson datatype: JSR310 — `com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.21.4`
- Jakarta Dependency Injection — `jakarta.inject:jakarta.inject-api:2.0.1`
- MapStruct Core — `org.mapstruct:mapstruct:1.6.3`
- micrometer-commons — `io.micrometer:micrometer-commons:1.17.0`
- micrometer-core — `io.micrometer:micrometer-core:1.17.0`
- micrometer-jakarta9 — `io.micrometer:micrometer-jakarta9:1.17.0`
- micrometer-observation — `io.micrometer:micrometer-observation:1.17.0`
- micrometer-registry-prometheus — `io.micrometer:micrometer-registry-prometheus:1.17.0`
- Prometheus Metrics Config — `io.prometheus:prometheus-metrics-config:1.5.1`
- Prometheus Metrics Core — `io.prometheus:prometheus-metrics-core:1.5.1`
- Prometheus Metrics Exposition Formats — `io.prometheus:prometheus-metrics-exposition-formats:1.5.1`
- Prometheus Metrics Exposition Text Formats — `io.prometheus:prometheus-metrics-exposition-textformats:1.5.1`
- Prometheus Metrics Model — `io.prometheus:prometheus-metrics-model:1.5.1`
- Prometheus Metrics Tracer Common — `io.prometheus:prometheus-metrics-tracer-common:1.5.1`
- thymeleaf — `org.thymeleaf:thymeleaf:3.1.5.RELEASE`
- thymeleaf-spring6 — `org.thymeleaf:thymeleaf-spring6:3.1.5.RELEASE`
- unbescape — `org.unbescape:unbescape:1.1.6.RELEASE`

### The MIT License

- Project Lombok — `org.projectlombok:lombok:1.18.46`
