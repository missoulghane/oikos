import { useEffect, useRef, type ReactNode } from 'react';

interface DropdownProps {
  isOpen: boolean;
  onClose: () => void;
  children: ReactNode;
  className?: string;
}

/**
 * Un panneau flottant, fermé dès qu'on clique en dehors de lui.
 *
 * <p>« En dehors » se mesure sur le conteneur positionné qui l'entoure, et non
 * sur le panneau seul : chaque appelant écrit
 * {@code <div className="relative"><button className="dropdown-toggle"/><Dropdown/></div>},
 * donc ce conteneur porte exactement le panneau et le bouton qui l'ouvre. Un
 * clic sur son propre bouton n'est donc pas « dehors » - c'est le onClick du
 * bouton qui referme, sans quoi les deux se battraient et le panneau
 * rouvrirait aussitôt.
 *
 * <p>La version précédente exemptait <em>tous</em> les {@code .dropdown-toggle}
 * de la page, pas seulement le sien : cliquer sur la cloche des messages
 * pendant que celle des notifications était ouverte laissait les deux panneaux
 * superposés. C'est la régression que ce périmètre corrige.
 */
export function Dropdown({ isOpen, onClose, children, className = '' }: DropdownProps) {
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      const group = dropdownRef.current?.parentElement;
      if (group && !group.contains(event.target as Node)) {
        onClose();
      }
    }

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [onClose]);

  if (!isOpen) return null;

  return (
    <div
      ref={dropdownRef}
      className={`absolute right-0 z-40 mt-2 rounded-xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-dark shadow-theme-lg ${className}`}
    >
      {children}
    </div>
  );
}
