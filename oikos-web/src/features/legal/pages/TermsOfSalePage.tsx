import { LegalDocument, LegalSection } from '@/features/legal/components/LegalDocument';
import { LEGAL_ENTITY as E } from '@/features/legal/constants/legalEntity';
import { TERMS_OF_USE_PATH } from '@/features/legal/constants/legalPaths';

/** Conditions générales de vente : la souscription aux services de la plateforme. */
export function TermsOfSalePage() {
  return (
    <LegalDocument
      title="Conditions générales de vente"
      related={{ label: "Conditions générales d'utilisation", to: TERMS_OF_USE_PATH }}
    >
      <LegalSection title="1. Objet">
        <p>
          Les présentes conditions générales de vente (les « CGV ») régissent la souscription aux services de la
          plateforme {E.productName} (les « Services ») par le syndic ou la copropriété qu'il représente (le
          « Client »). Elles prévalent sur tout autre document, sauf accord écrit contraire, et complètent les
          conditions générales d'utilisation.
        </p>
      </LegalSection>

      <LegalSection title="2. Prestataire">
        <p>
          Les Services sont fournis par {E.companyName}, {E.legalForm}, siège {E.headOffice}, RC {E.tradeRegister},
          ICE {E.ice}, IF {E.taxId} (le « Prestataire »). Contact : {E.contactEmail}.
        </p>
      </LegalSection>

      <LegalSection title="3. Services et offres">
        <p>
          Les Services consistent en la mise à disposition en ligne de la plateforme de gestion de copropriété
          décrite dans les conditions générales d'utilisation, de ses mises à jour et de l'assistance associée. Le
          contenu de chaque offre, ses limites (nombre de copropriétés, de lots, d'utilisateurs) et son prix sont
          présentés au Client avant toute souscription : [Grille des offres et des tarifs à compléter].
        </p>
      </LegalSection>

      <LegalSection title="4. Prix">
        <p>
          Les prix sont exprimés en dirhams marocains (MAD), hors taxes et toutes taxes comprises, la TVA étant
          appliquée au taux en vigueur. Le Prestataire peut faire évoluer ses tarifs ; le nouveau tarif ne
          s'applique qu'à la période suivante, après information du Client au moins [30] jours avant son entrée en
          vigueur.
        </p>
      </LegalSection>

      <LegalSection title="5. Souscription">
        <p>
          La souscription s'effectue en ligne. Avant de la valider, le Client peut vérifier le détail de sa
          commande et son prix total, et corriger d'éventuelles erreurs ; il reçoit ensuite une confirmation
          électronique de son engagement. Le contrat est conclu par voie électronique, conformément à la loi
          n° 53-05 relative à l'échange électronique de données juridiques.
        </p>
      </LegalSection>

      <LegalSection title="6. Facturation et paiement">
        <p>
          Les Services sont facturés [mensuellement / annuellement], terme à échoir, selon les modalités de paiement
          proposées lors de la souscription. Les factures sont mises à disposition sous forme électronique. Tout
          retard de paiement peut entraîner, après mise en demeure restée sans effet, la suspension des Services,
          ainsi que les pénalités prévues par la réglementation relative aux délais de paiement lorsque le Client
          agit à titre professionnel.
        </p>
      </LegalSection>

      <LegalSection title="7. Durée et résiliation">
        <p>
          L'abonnement est conclu pour la durée choisie lors de la souscription et se renouvelle tacitement pour
          une durée identique, sauf résiliation par l'une des parties au moins [30] jours avant son échéance. Le
          Prestataire rappelle cette faculté au Client avant chaque renouvellement. En cas de manquement grave de
          l'une des parties, l'autre peut résilier le contrat après mise en demeure restée sans effet pendant
          [15] jours.
        </p>
      </LegalSection>

      <LegalSection title="8. Droit de rétractation">
        <p>
          Lorsque le Client a la qualité de consommateur au sens de la loi n° 31-08 édictant des mesures de
          protection du consommateur, il dispose d'un délai de sept jours à compter de la souscription pour se
          rétracter, sans avoir à se justifier ni à payer de pénalité, en écrivant à {E.contactEmail}. Si le Client
          a expressément demandé que l'exécution des Services commence avant la fin de ce délai, les Services déjà
          fournis restent dus, dans les limites prévues par la loi.
        </p>
      </LegalSection>

      <LegalSection title="9. Niveau de service et assistance">
        <p>
          Le Prestataire assure l'hébergement, la maintenance et la sauvegarde régulière des données. L'assistance
          est accessible à {E.contactEmail} [jours et horaires à préciser]. Les opérations de maintenance planifiées
          sont, dans la mesure du possible, annoncées à l'avance et réalisées en dehors des heures ouvrables.
        </p>
      </LegalSection>

      <LegalSection title="10. Responsabilité">
        <p>
          Le Prestataire est tenu d'une obligation de moyens. Sauf faute lourde ou dolosive, sa responsabilité est
          limitée aux dommages directs et plafonnée au montant payé par le Client au titre des douze derniers mois.
          Il n'est pas responsable des données saisies par le Client ni de l'usage qui en est fait.
        </p>
      </LegalSection>

      <LegalSection title="11. Données et réversibilité">
        <p>
          Le traitement des données à caractère personnel est décrit dans les conditions générales d'utilisation.
          Les données du Client restent sa propriété : à la fin du contrat, il peut en obtenir l'export dans un
          format exploitable pendant [30] jours, à l'issue desquels elles sont supprimées, sous réserve des
          obligations légales de conservation.
        </p>
      </LegalSection>

      <LegalSection title="12. Force majeure">
        <p>
          Aucune partie n'est responsable d'un manquement causé par un cas de force majeure au sens du dahir
          formant code des obligations et des contrats. Si l'empêchement dure plus de [30] jours, chaque partie
          peut résilier le contrat sans indemnité.
        </p>
      </LegalSection>

      <LegalSection title="13. Droit applicable et litiges">
        <p>
          Les CGV sont régies par le droit marocain. Les parties recherchent d'abord une solution amiable ; à
          défaut, le litige est porté devant les tribunaux compétents de {E.competentCourt}, sous réserve des
          règles impératives de compétence applicables lorsque le Client est un consommateur.
        </p>
      </LegalSection>
    </LegalDocument>
  );
}
