import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/auth.guard';

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
    canActivate: [roleGuard('PUBLISHER', 'ADMIN')],
    loadComponent: () => import('./features/documents/upload.component').then((m) => m.UploadComponent),
  },
  {
    path: 'viewer/:documentId',
    canActivate: [authGuard],
    loadComponent: () => import('./features/viewer/viewer.component').then((m) => m.ViewerComponent),
  },
  {
    path: 'admin',
    canActivate: [roleGuard('ADMIN')],
    loadComponent: () =>
      import('./features/admin/admin-dashboard.component').then((m) => m.AdminDashboardComponent),
  },
  {
    path: 'account',
    canActivate: [authGuard],
    loadComponent: () => import('./features/auth/account.component').then((m) => m.AccountComponent),
  },
  { path: '**', redirectTo: 'documents' },
];
