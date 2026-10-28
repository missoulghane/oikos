import { LegalDocument, LegalSection } from '@/features/legal/components/LegalDocument';
import { LEGAL_ENTITY as E } from '@/features/legal/constants/legalEntity';
import { TERMS_OF_SALE_PATH } from '@/features/legal/constants/legalPaths';

/** Conditions générales d'utilisation : l'usage de la plateforme, quel que soit le compte. */
export function TermsOfUsePage() {
  return (
    <LegalDocument
      title="Conditions générales d'utilisation"
      related={{ label: 'Conditions générales de vente', to: TERMS_OF_SALE_PATH }}
    >
      <LegalSection title="1. Objet">
        <p>
          Les présentes conditions générales d'utilisation (les « CGU ») encadrent l'accès à la plateforme{' '}
          {E.productName}, accessible sur {E.website} et sur ses applications mobiles (la « Plateforme »), et son
          utilisation par toute personne qui y crée un compte ou y est invitée (l'« Utilisateur »). Elles sont
          complétées, pour les services souscrits à titre onéreux, par les conditions générales de vente.
        </p>
      </LegalSection>

      <LegalSection title="2. Éditeur de la Plateforme">
        <p>
          La Plateforme est éditée par {E.companyName}, {E.legalForm}, dont le siège est situé {E.headOffice},
          immatriculée au registre du commerce sous le n° {E.tradeRegister}, ICE {E.ice}, IF {E.taxId}.
          Responsable de la publication : {E.publicationDirector}. Hébergement : {E.host}. Contact : {E.contactEmail}.
        </p>
      </LegalSection>

      <LegalSection title="3. Description du service">
        <p>
          La Plateforme est un logiciel de gestion de copropriété fourni en ligne (SaaS). Elle permet notamment de
          décrire une copropriété et ses lots, d'émettre et de suivre les appels de fonds, de tenir la comptabilité
          de la copropriété, de préparer et convoquer les assemblées générales, d'échanger avec les copropriétaires
          et de partager des documents.
        </p>
        <p>
          La Plateforme est un outil d'aide à la gestion. Le syndic et le conseil syndical demeurent seuls
          responsables du respect des obligations que leur impose la loi n° 18-00 relative au statut de la
          copropriété des immeubles bâtis, telle que modifiée et complétée, ainsi que le règlement de copropriété.
        </p>
      </LegalSection>

      <LegalSection title="4. Accès et compte utilisateur">
        <p>
          L'inscription directe est réservée au syndic et aux membres du conseil syndical d'une copropriété ; les
          copropriétaires accèdent à la Plateforme sur invitation. L'Utilisateur fournit des informations exactes
          et les tient à jour. Ses identifiants sont personnels et confidentiels : toute action effectuée depuis
          son compte est réputée l'avoir été par lui, jusqu'à ce qu'il signale une utilisation frauduleuse à{' '}
          {E.contactEmail}.
        </p>
      </LegalSection>

      <LegalSection title="5. Obligations de l'Utilisateur">
        <p>L'Utilisateur s'engage à :</p>
        <ul className="list-disc pl-5">
          <li>n'utiliser la Plateforme que pour la gestion de la ou des copropriétés pour lesquelles il est habilité ;</li>
          <li>ne saisir que des données qu'il est en droit de traiter, et informer les personnes concernées ;</li>
          <li>ne pas porter atteinte à la sécurité ou au fonctionnement de la Plateforme ;</li>
          <li>ne publier aucun contenu illicite, diffamatoire ou portant atteinte aux droits de tiers.</li>
        </ul>
      </LegalSection>

      <LegalSection title="6. Protection des données à caractère personnel">
        <p>
          Les données à caractère personnel sont traitées conformément à la loi n° 09-08 relative à la protection
          des personnes physiques à l'égard du traitement des données à caractère personnel. Le traitement a fait
          l'objet de la formalité préalable auprès de la Commission nationale de contrôle de la protection des
          données à caractère personnel (CNDP) sous le n° {E.cndpDeclaration}.
        </p>
        <p>
          Les données sont traitées pour fournir le service (gestion du compte, de la copropriété, des appels de
          fonds, des assemblées générales et des échanges), en assurer la sécurité et respecter les obligations
          légales de l'éditeur. Elles sont destinées aux seuls personnels habilités de l'éditeur, à ses
          sous-traitants techniques et aux membres de la copropriété dans la limite de leurs droits. Elles sont
          conservées pendant la durée du compte, puis le temps des obligations légales de conservation. Tout
          transfert hors du Maroc est soumis aux conditions de la loi n° 09-08.
        </p>
        <p>
          L'Utilisateur dispose d'un droit d'accès, de rectification et d'opposition, pour motifs légitimes, au
          traitement de ses données, qu'il exerce en écrivant à {E.contactEmail}.
        </p>
        <p>
          Lorsqu'il saisit les données des copropriétaires et des tiers de sa copropriété, le syndic agit en tant
          que responsable de ce traitement et l'éditeur en tant que sous-traitant, qui ne les utilise que pour
          fournir le service.
        </p>
      </LegalSection>

      <LegalSection title="7. Propriété intellectuelle">
        <p>
          La Plateforme, ses logiciels, sa marque et ses contenus sont la propriété de l'éditeur. L'Utilisateur
          bénéficie d'un droit d'usage personnel et non exclusif, limité à la durée de son compte. Les données
          saisies par l'Utilisateur restent sa propriété ou celle de la copropriété.
        </p>
      </LegalSection>

      <LegalSection title="8. Disponibilité et responsabilité">
        <p>
          L'éditeur met en œuvre les moyens raisonnables pour assurer l'accès à la Plateforme et la sécurité des
          données, sans garantir une disponibilité ininterrompue : des interruptions peuvent survenir pour
          maintenance ou en cas d'incident. L'éditeur n'est pas responsable de l'exactitude des données saisies par
          les Utilisateurs, ni des décisions prises sur leur fondement.
        </p>
      </LegalSection>

      <LegalSection title="9. Suspension et clôture du compte">
        <p>
          L'éditeur peut suspendre un compte en cas de manquement grave aux présentes CGU, après en avoir informé
          l'Utilisateur sauf urgence. L'Utilisateur peut demander la clôture de son compte à tout moment à{' '}
          {E.contactEmail}.
        </p>
      </LegalSection>

      <LegalSection title="10. Preuve et acceptation électronique">
        <p>
          L'acceptation des CGU par case à cocher lors de l'inscription vaut consentement, conformément à la
          législation relative à l'échange électronique de données juridiques et aux services de confiance pour
          les transactions électroniques (lois n° 53-05 et n° 43-20). Les enregistrements conservés par l'éditeur
          font foi, sauf preuve contraire.
        </p>
      </LegalSection>

      <LegalSection title="11. Modification des CGU">
        <p>
          L'éditeur peut modifier les CGU. Les Utilisateurs sont informés de toute modification substantielle avant
          son entrée en vigueur ; la poursuite de l'utilisation vaut acceptation de la nouvelle version.
        </p>
      </LegalSection>

      <LegalSection title="12. Droit applicable et litiges">
        <p>
          Les CGU sont régies par le droit marocain. Les parties recherchent d'abord une solution amiable ; à
          défaut, le litige est porté devant les tribunaux compétents de {E.competentCourt}, sous réserve des
          règles impératives applicables aux consommateurs.
        </p>
      </LegalSection>
    </LegalDocument>
  );
}
