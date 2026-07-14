import { ErrorBoundary } from '@/shared/components/ErrorBoundary/ErrorBoundary';
import { AppProviders } from '@/app/providers';
import { AppRouter } from '@/router';

export function App() {
  return (
    <ErrorBoundary>
      <AppProviders>
        <AppRouter />
      </AppProviders>
    </ErrorBoundary>
  );
}
