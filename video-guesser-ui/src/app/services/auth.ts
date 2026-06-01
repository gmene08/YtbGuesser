import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';
import { UserResponse } from '../dtos/auth.dto';


@Injectable({
  providedIn: 'root',
})

export class Auth {
  private apiUrl = '/api/users';

  constructor(private http: HttpClient) {}

  checkSession(){
    return this.http.get<UserResponse>(`${this.apiUrl}/me`);
  }

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
