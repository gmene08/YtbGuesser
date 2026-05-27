import { HttpInterceptorFn } from '@angular/common/http';

export const cookieInterceptor: HttpInterceptorFn = (req, next) => {
  // clone the request to add the new header
  const secureReq = req.clone({
    withCredentials: true,
  });

  return next(secureReq);
};
