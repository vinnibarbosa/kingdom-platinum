import { HttpErrorResponse, HttpEvent, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, switchMap, throwError } from 'rxjs';

import { AuthService } from '../services/auth.service';

const API_PREFIX = '/api/';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!request.url.startsWith(API_PREFIX)) {
    return next(request);
  }

  const skipsAuthentication = /\/auth\/(?:login|registrar|refresh)(?:$|\?)/.test(request.url);
  const send = (token?: string | null): Observable<HttpEvent<unknown>> => next(withAuthentication(request, token));
  const endSession = (error: unknown): Observable<never> => {
    auth.clearSession();
    void router.navigateByUrl('/login');
    return throwError(() => error);
  };
  const refreshAndRetry = (): Observable<HttpEvent<unknown>> => auth.refreshAccessToken().pipe(
    switchMap((token) => send(token)),
    catchError((error) => endSession(error)),
  );

  if (!skipsAuthentication && auth.token && auth.shouldRefreshAccessToken()) {
    return refreshAndRetry();
  }

  return send(skipsAuthentication ? null : auth.token).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status !== 401 || skipsAuthentication || !auth.token) {
        return throwError(() => error);
      }

      return refreshAndRetry();
    }),
  );
};

function withAuthentication(request: HttpRequest<unknown>, token?: string | null): HttpRequest<unknown> {
  return request.clone({
    withCredentials: true,
    setHeaders: token ? { Authorization: `Bearer ${token}` } : {},
  });
}
