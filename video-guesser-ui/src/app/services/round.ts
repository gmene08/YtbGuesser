import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { UserGuessRequest, UserGuessResponse } from '../dtos/round.dto';

@Injectable({
  providedIn: 'root',
})
export class RoundService {
  private apiUrl = 'http://localhost:8080/api/round';

  constructor(private http: HttpClient) {}

  guess(roundId: number, userGuess: UserGuessRequest) {
    return this.http.post<UserGuessResponse>(`${this.apiUrl}/${roundId}/guess`, userGuess);
  }
}
