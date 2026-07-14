import { Component, type ErrorInfo, type PropsWithChildren } from 'react';
import { Button } from '@/shared/components/Button/Button';

interface ErrorBoundaryState {
  hasError: boolean;
}

/**
 * Last-resort fallback for render errors. React error boundaries only work
 * as class components (no hook equivalent exists yet).
 */
export class ErrorBoundary extends Component<PropsWithChildren, ErrorBoundaryState> {
  state: ErrorBoundaryState = { hasError: false };

  static getDerivedStateFromError(): ErrorBoundaryState {
    return { hasError: true };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Unhandled render error', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="flex min-h-screen flex-col items-center justify-center gap-4 px-4 text-center">
          <h1 className="text-lg font-semibold text-slate-900">Une erreur est survenue</h1>
          <p className="text-sm text-slate-500">Veuillez recharger la page.</p>
          <Button onClick={() => window.location.reload()}>Recharger</Button>
        </div>
      );
    }

    return this.props.children;
  }
}
