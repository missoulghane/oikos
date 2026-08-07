// Mirrors RegisterUserService's RETURN_TO_PATTERN (oikos-api) - defense in
// depth, not a substitute for the backend check: a hand-crafted verify link
// could bypass the registration form entirely. A single leading '/' not
// followed by another '/' rules out "//evil.com" (browser-parsed as
// protocol-relative, an open-redirect vector) and any absolute URL.
const RETURN_TO_PATTERN = /^\/(?!\/).*/;

export function sanitizeReturnTo(returnTo: string | null | undefined): string | null {
  if (!returnTo || !RETURN_TO_PATTERN.test(returnTo)) {
    return null;
  }
  return returnTo;
}
