import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { UserGuessRequest } from '../dtos/round.dto';

@Injectable({
  providedIn: 'root',
})
export class GameService {
  private apiUrl = 'http://localhost:8080/api/game';

  constructor(private http: HttpClient) {}


  changeRoundStatus(roundId: number, status: string) {
    return this.http.patch(`${this.apiUrl}/${roundId}/status`, { status: status });
  }
}
