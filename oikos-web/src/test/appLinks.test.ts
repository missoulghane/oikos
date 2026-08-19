import { describe, expect, it } from 'vitest';
// Importés en `?raw` plutôt que lus avec node:fs : le tsconfig de l'app
// n'expose que les types du navigateur (types: ["vite/client"]), et un test
// n'est pas une raison d'y faire entrer ceux de Node.
import appleAppSiteAssociation from '../../public/.well-known/apple-app-site-association?raw';
import androidAssetLinks from '../../public/.well-known/assetlinks.json?raw';

/**
 * Les deux fichiers d'association servis sous /.well-known décident si un lien
 * reçu par email ouvre l'application mobile ou le navigateur. Rien ne les
 * exerce à l'exécution : ils sont lus par Apple et Google, une fois, à
 * l'installation. Un JSON invalide ou un identifiant mal recopié ne se voit
 * donc nulle part - l'app s'ouvre simplement « comme avant », et personne ne
 * cherche une panne qui ressemble au comportement d'hier.
 */

const BUNDLE_ID = 'com.issoulghane.oikosmobile';

/** Les chemins des liens envoyés par email (voir oikos.mail.*-base-url côté API). */
const LINKED_PATHS = ['/reset-password', '/verify-email', '/activate-account', '/accept-invitation', '/invitations'];

describe('apple-app-site-association', () => {
  const aasa = JSON.parse(appleAppSiteAssociation) as {
    applinks: { details: { appIDs: string[]; components: { '/': string; exclude?: boolean }[] }[] };
  };

  it("désigne le bundle de l'app mobile", () => {
    const [detail] = aasa.applinks.details;

    expect(detail.appIDs).toHaveLength(1);
    expect(detail.appIDs[0]).toMatch(new RegExp(`\\.${BUNDLE_ID}$`));
  });

  it('ouvre exactement les chemins portés par les emails', () => {
    const [detail] = aasa.applinks.details;
    const opened = detail.components.filter((component) => !component.exclude).map((component) => component['/']);

    expect(opened).toEqual(LINKED_PATHS.map((path) => `${path}*`));
  });

  it('laisse tout le reste au navigateur', () => {
    // La confirmation de convocation en particulier : elle existe pour les
    // copropriétaires qui n'ont pas de compte et n'auront pas l'app.
    const [detail] = aasa.applinks.details;
    const last = detail.components.at(-1);

    expect(last).toMatchObject({ '/': '*', exclude: true });
  });
});

describe('assetlinks.json', () => {
  const assetLinks = JSON.parse(androidAssetLinks) as {
    relation: string[];
    target: { namespace: string; package_name: string; sha256_cert_fingerprints: string[] };
  }[];

  it("désigne le paquet de l'app mobile", () => {
    expect(assetLinks).toHaveLength(1);
    expect(assetLinks[0].target).toMatchObject({ namespace: 'android_app', package_name: BUNDLE_ID });
    expect(assetLinks[0].relation).toEqual(['delegate_permission/common.handle_all_urls']);
  });

  it('porte une empreinte de certificat', () => {
    // Le contenu ne peut pas être vérifié ici (il vient des identifiants de
    // signature), seulement le fait qu'il y en ait exactement une.
    expect(assetLinks[0].target.sha256_cert_fingerprints).toHaveLength(1);
  });
});
