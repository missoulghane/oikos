import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/app/store';
import { useCurrentUser } from '@/features/identity/me';
import { useEffectiveSpace, spaceQuerySuffix } from '@/shared/hooks/useEffectiveSpace';
import { Dropdown } from '@/shared/components/Dropdown/Dropdown';

export function UserDropdown() {
  const [isOpen, setIsOpen] = useState(false);
  const navigate = useNavigate();
  const currentUser = useCurrentUser();
  const clearSession = useAuthStore((state) => state.clearSession);
  // Carried onto "Mes informations" so opening the profile from the board
  // space doesn't silently drop the viewer back into owner (see
  // spaceQuerySuffix) - /profile itself doesn't care, but the sidebar
  // recomputes the effective space from the URL on every route.
  const profileHref = `/profile${spaceQuerySuffix(useEffectiveSpace())}`;

  function handleLogout() {
    clearSession();
    navigate('/login', { replace: true });
  }

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen((value) => !value)}
        className="dropdown-toggle flex items-center gap-2 text-gray-700 dark:text-gray-300"
      >
        <span className="flex h-9 w-9 items-center justify-center rounded-full bg-brand-50 dark:bg-brand-500/[0.12] text-sm font-medium text-brand-600 dark:text-brand-400">
          {currentUser.data?.fullName.charAt(0).toUpperCase() ?? '?'}
        </span>
        <span className="hidden text-sm font-medium sm:block">{currentUser.data?.fullName}</span>
        <svg
          className={`hidden stroke-gray-500 dark:stroke-gray-400 transition-transform duration-200 sm:block ${isOpen ? 'rotate-180' : ''}`}
          width="18"
          height="20"
          viewBox="0 0 18 20"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M4.3125 8.65625L9 13.3437L13.6875 8.65625"
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
      </button>

      <Dropdown isOpen={isOpen} onClose={() => setIsOpen(false)} className="flex w-[220px] flex-col p-3">
        <div className="border-b border-gray-200 dark:border-gray-800 pb-3">
          <span className="block text-sm font-medium text-gray-700 dark:text-gray-300">{currentUser.data?.fullName}</span>
          <span className="mt-0.5 block text-xs text-gray-500 dark:text-gray-400">{currentUser.data?.email}</span>
        </div>
        <Link
          to={profileHref}
          onClick={() => setIsOpen(false)}
          className="mt-3 flex items-center gap-2 rounded-lg px-3 py-2 text-left text-sm font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-white/[0.05]"
        >
          Mes informations
        </Link>
        <button
          onClick={handleLogout}
          className="mt-3 flex items-center gap-2 rounded-lg px-3 py-2 text-left text-sm font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-white/[0.05]"
        >
          Se déconnecter
        </button>
      </Dropdown>
    </div>
  );
}
