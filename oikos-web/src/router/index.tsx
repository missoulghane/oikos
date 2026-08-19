import { createBrowserRouter, Outlet, RouterProvider } from 'react-router-dom';
import { publicRoutes } from '@/router/publicRoutes';
import { privateRoutes } from '@/router/privateRoutes';
import { ProtectedRoute } from '@/router/ProtectedRoute';
import { AppErrorPage } from '@/shared/pages/AppErrorPage';
import { NotFoundPage } from '@/shared/pages/NotFoundPage';

// Une route parente sans chemin, dont le seul rôle est de porter l'errorElement :
// React Router ne remonte une erreur de rendu que jusqu'à la route la plus proche
// qui en déclare un, et sans parent commun chaque page tombant en panne laissait
// l'écran blanc.
const router = createBrowserRouter([
  {
    element: <Outlet />,
    errorElement: <AppErrorPage />,
    children: [
      ...publicRoutes,
      { element: <ProtectedRoute />, children: privateRoutes },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);

export function AppRouter() {
  return <RouterProvider router={router} />;
}
