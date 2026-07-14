import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { publicRoutes } from '@/router/publicRoutes';
import { privateRoutes } from '@/router/privateRoutes';
import { ProtectedRoute } from '@/router/ProtectedRoute';
import { NotFoundPage } from '@/shared/pages/NotFoundPage';

const router = createBrowserRouter([
  ...publicRoutes,
  { element: <ProtectedRoute />, children: privateRoutes },
  { path: '*', element: <NotFoundPage /> },
]);

export function AppRouter() {
  return <RouterProvider router={router} />;
}
