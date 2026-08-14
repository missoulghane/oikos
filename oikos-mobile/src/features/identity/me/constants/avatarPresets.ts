export interface AvatarPreset {
  id: string;
  source: number;
  label: string;
}

export const AVATAR_PRESETS: AvatarPreset[] = [
  { id: 'homme-1', source: require('@/shared/assets/avatars/homme-1.png'), label: 'Avatar homme bleu' },
  { id: 'homme-2', source: require('@/shared/assets/avatars/homme-2.png'), label: 'Avatar homme vert' },
  { id: 'homme-3', source: require('@/shared/assets/avatars/homme-3.png'), label: 'Avatar homme orange' },
  { id: 'femme-1', source: require('@/shared/assets/avatars/femme-1.png'), label: 'Avatar femme rose' },
  { id: 'femme-2', source: require('@/shared/assets/avatars/femme-2.png'), label: 'Avatar femme violet' },
  { id: 'femme-3', source: require('@/shared/assets/avatars/femme-3.png'), label: 'Avatar femme rouge' },
  { id: 'metier-chantier', source: require('@/shared/assets/avatars/metier-chantier.png'), label: 'Avatar chantier' },
  { id: 'metier-cuisine', source: require('@/shared/assets/avatars/metier-cuisine.png'), label: 'Avatar cuisine' },
  { id: 'metier-bureau', source: require('@/shared/assets/avatars/metier-bureau.png'), label: 'Avatar bureau' },
  { id: 'metier-accueil', source: require('@/shared/assets/avatars/metier-accueil.png'), label: 'Avatar accueil' },
  { id: 'neutre-1', source: require('@/shared/assets/avatars/neutre-1.png'), label: 'Avatar neutre clair' },
  { id: 'neutre-2', source: require('@/shared/assets/avatars/neutre-2.png'), label: 'Avatar neutre foncé' },
];
