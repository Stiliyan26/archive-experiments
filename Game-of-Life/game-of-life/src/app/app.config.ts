import {
  ApplicationConfig,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withFetch } from '@angular/common/http';

import { routes } from './app.routes';
import { API_URL } from './core/api/api-url.token';
import { environment } from '../environments/environment.development';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(), // Catch browser errors outside of zone, handled by the event loop like setTimeout
    provideZoneChangeDetection({ eventCoalescing: true }), // Merge in one change detection cycle
    provideRouter(routes), 
    provideHttpClient(withFetch()),
    { provide: API_URL, useValue: environment.apiUrl },
  ],
};
