import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'documents' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'documents',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/documents/document-list.component').then((m) => m.DocumentListComponent),
  },
  {
    path: 'documents/upload',
    canActivate: [authGuard],
    loadComponent: () => import('./features/documents/upload.component').then((m) => m.UploadComponent),
  },
  {
    path: 'viewer/:documentId',
    canActivate: [authGuard],
    loadComponent: () => import('./features/viewer/viewer.component').then((m) => m.ViewerComponent),
  },
  {
    path: 'admin',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/admin/admin-dashboard.component').then((m) => m.AdminDashboardComponent),
  },
  { path: '**', redirectTo: 'documents' },
];
