export interface AvatarPreset {
  id: string;
  url: string;
  label: string;
}

export const AVATAR_PRESETS: AvatarPreset[] = [
  { id: 'homme-1', url: '/images/avatars/homme-1.png', label: 'Avatar homme bleu' },
  { id: 'homme-2', url: '/images/avatars/homme-2.png', label: 'Avatar homme vert' },
  { id: 'homme-3', url: '/images/avatars/homme-3.png', label: 'Avatar homme orange' },
  { id: 'femme-1', url: '/images/avatars/femme-1.png', label: 'Avatar femme rose' },
  { id: 'femme-2', url: '/images/avatars/femme-2.png', label: 'Avatar femme violet' },
  { id: 'femme-3', url: '/images/avatars/femme-3.png', label: 'Avatar femme rouge' },
  { id: 'metier-chantier', url: '/images/avatars/metier-chantier.png', label: 'Avatar chantier' },
  { id: 'metier-cuisine', url: '/images/avatars/metier-cuisine.png', label: 'Avatar cuisine' },
  { id: 'metier-bureau', url: '/images/avatars/metier-bureau.png', label: 'Avatar bureau' },
  { id: 'metier-accueil', url: '/images/avatars/metier-accueil.png', label: 'Avatar accueil' },
  { id: 'neutre-1', url: '/images/avatars/neutre-1.png', label: 'Avatar neutre clair' },
  { id: 'neutre-2', url: '/images/avatars/neutre-2.png', label: 'Avatar neutre foncé' },
];
