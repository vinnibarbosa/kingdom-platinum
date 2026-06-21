import { Routes } from '@angular/router';

import { authGuard } from './guards/auth.guard';
import { FichaPageComponent } from './pages/ficha/ficha-page.component';
import { FichaListPageComponent } from './pages/ficha-list/ficha-list-page.component';
import { FichaViewPageComponent } from './pages/ficha-view/ficha-view-page.component';
import { LoginPageComponent } from './pages/login/login-page.component';

export const routes: Routes = [
  { path: 'login', component: LoginPageComponent },
  { path: '', component: FichaListPageComponent, canActivate: [authGuard] },
  { path: 'ficha/:id/visualizar', component: FichaViewPageComponent },
  { path: 'ficha/:id', component: FichaPageComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: '' },
];
