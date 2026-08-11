import type { PropsWithChildren } from 'react';
import { ThemeToggleButton } from '@/shared/components/ThemeToggleButton/ThemeToggleButton';

function GridShape() {
  return (
    <>
      <div className="absolute right-0 top-0 -z-1 w-full max-w-[250px] xl:max-w-[450px]">
        <img src="/images/shape/grid-01.svg" alt="" />
      </div>
      <div className="absolute bottom-0 left-0 -z-1 w-full max-w-[250px] rotate-180 xl:max-w-[450px]">
        <img src="/images/shape/grid-01.svg" alt="" />
      </div>
    </>
  );
}

interface AuthLayoutProps {
  /**
   * Écrans en plusieurs étapes (wizard d'inscription) : carte plus large, et
   * hauteur minimale plutôt que fixe, sinon une étape longue (liste de
   * bâtiments) déborde sans pouvoir défiler.
   */
  wide?: boolean;
}

export function AuthLayout({ children, wide = false }: PropsWithChildren<AuthLayoutProps>) {
  return (
    <div className="relative z-1 bg-white dark:bg-gray-900 p-6 sm:p-0">
      <div className={`relative flex w-full flex-col justify-center lg:flex-row ${wide ? 'min-h-screen py-8' : 'h-screen'}`}>
        <div className="flex w-full flex-1 flex-col justify-center">
          <div className={`mx-auto flex w-full justify-center ${wide ? 'max-w-2xl px-4' : 'max-w-sm'}`}>
            <div className="w-full">
              <h1 className="mb-6 text-center text-xl font-semibold text-gray-900 dark:text-white/90">Oikos</h1>
              {children}
            </div>
          </div>
        </div>
        {/* Panneau décoratif masqué en mode `wide` : un formulaire en 7 étapes a
            besoin de toute la largeur, il n'y a rien à sacrifier pour un visuel. */}
        <div className={`w-full items-center bg-brand-950 lg:w-1/2 ${wide ? 'hidden' : 'hidden lg:grid'}`}>
          <div className="relative z-1 flex items-center justify-center">
            <GridShape />
            <div className="flex max-w-xs flex-col items-center">
              <span className="mb-4 block text-2xl font-semibold text-white">Oikos</span>
              <p className="text-center text-gray-400 dark:text-gray-500">Gestion de copropriété simplifiée</p>
            </div>
          </div>
        </div>
        {/* Auth screens render outside the app shell, so they carry their own
            switch - otherwise the choice could only be made once logged in. */}
        <div className="fixed bottom-6 right-6 z-50">
          <ThemeToggleButton />
        </div>
      </div>
    </div>
  );
}
