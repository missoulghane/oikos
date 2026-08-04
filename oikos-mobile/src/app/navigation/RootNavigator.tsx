import { NavigationContainer } from '@react-navigation/native';
import { useAuthStore } from '@/app/store';
import { AuthNavigator } from '@/app/navigation/AuthNavigator';
import { MainNavigator } from '@/app/navigation/MainNavigator';
import { Loader } from '@/shared/components/Loader/Loader';

/**
 * Chooses between the Auth and Main stacks based on isAuthenticated - the
 * mobile equivalent of oikos-web's ProtectedRoute. It reacts to the store
 * synchronously, so clearSession() (whether from an explicit logout or a
 * failed token refresh in httpClient.ts) switches back to Auth on its own,
 * with no imperative navigation call needed from the call site.
 */
export function RootNavigator() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const hasHydrated = useAuthStore((state) => state.hasHydrated);

  if (!hasHydrated) {
    return <Loader label="Chargement de la session…" />;
  }

  return <NavigationContainer>{isAuthenticated ? <MainNavigator /> : <AuthNavigator />}</NavigationContainer>;
}
