import { ApplicationConfig, provideBrowserGlobalErrorListeners, LOCALE_ID } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { cookieInterceptor } from './cookie/cookie.interceptor';

import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import localeEn from '@angular/common/locales/en';
import localeEs from '@angular/common/locales/es';
registerLocaleData(localePt);
registerLocaleData(localeEn);
registerLocaleData(localeEs);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([cookieInterceptor])),
    { provide: LOCALE_ID, useFactory: () => {
      const browserLang = navigator.language.split('-')[0];
      if (browserLang === 'pt') return 'pt';
      if (browserLang === 'en') return 'en';
      if (browserLang === 'es') return 'es';
      return 'en';
      }},
  ],
};
