import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatchDataResponse } from '../dtos/match.dto';
import { UserGuessRequest, UserGuessResponse } from '../dtos/round.dto';

@Injectable({
  providedIn: 'root',
})
export class MatchService {
  private apiUrl = 'http://localhost:8080/api/match';

  constructor(private http: HttpClient) {}

  getMatchDataByRoomCode(roomCode: string) {
    return this.http.get<MatchDataResponse>(`${this.apiUrl}/?roomCode=${roomCode}`);
  }

  getMatchDataByMatchId(matchId: number) {
    return this.http.get<MatchDataResponse>(`${this.apiUrl}/${matchId}`);
  }

}
