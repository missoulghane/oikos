import { Outlet, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/app/store';
import { Button } from '@/shared/components/Button/Button';

export function AppLayout() {
  const navigate = useNavigate();
  const clearSession = useAuthStore((state) => state.clearSession);

  function handleLogout() {
    clearSession();
    navigate('/login', { replace: true });
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-4xl items-center justify-between px-4 py-3 sm:px-6">
          <span className="text-base font-semibold text-slate-900">Oikos</span>
          <Button variant="secondary" onClick={handleLogout}>
            Se déconnecter
          </Button>
        </div>
      </header>
      <main className="mx-auto max-w-4xl px-4 py-6 sm:px-6">
        <Outlet />
      </main>
    </div>
  );
}
