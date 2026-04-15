import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';

export interface UserResponse {
  id: number;
  nickname: string;
  email: string;
  isGuest: boolean;
}

@Injectable({
  providedIn: 'root',
})

export class Auth {
  private apiUrl = 'http://localhost:8080/api/users';

  constructor(private http: HttpClient) {}

  createGuest(nickname: string) {
    return this.http.post<UserResponse>(`${this.apiUrl}/guest`, { nickname: nickname }).pipe(
      tap((response) => {
        sessionStorage.setItem('userId', response.id.toString());
        sessionStorage.setItem('nickname', response.nickname);
        console.log('Guest ID saved in session:', response.id.toString());
      }),
    );
  }

  login(nickname: string, password: string) {
    return this.http.post<UserResponse>(`${this.apiUrl}/login`, { nickname: nickname, password: password }).pipe(tap((response) => {
      sessionStorage.setItem('userId', response.id.toString());
      sessionStorage.setItem('nickname', response.nickname);
      console.log('User ID saved in session: ', response.id.toString());
    }));
  }
}
