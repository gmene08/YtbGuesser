import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class Auth {
  private apiUrl = 'http://localhost:8080/api/users';

  constructor(private http: HttpClient) { }

  createGuest(nickname: string){
    return this.http.post(`${this.apiUrl}/guest`, { nickname: nickname });
  }
}
