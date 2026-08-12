import { Navigate } from 'react-router-dom';

/**
 * Entry point for "/": always the dashboard, whatever the account's state -
 * a constant landing screen is learnable, a variable one has to be endured.
 * DashboardPage itself resolves the right default content from there
 * (copropriétaire consolidé dès qu'un lot existe, sinon le premier mandat,
 * sinon gérant) - this route must never shortcut past it, even for a staff
 * account managing (or a board member sitting on) exactly one property: that
 * used to jump straight to /property-mngt/properties/{id}, skipping Accueil
 * entirely and contradicting the prototype's landing rule.
 */
export function LandingPage() {
  return <Navigate to="/dashboard" replace />;
}
