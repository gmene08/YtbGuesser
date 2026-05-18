import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatchDataResponse } from '../dtos/match.dto';

@Injectable({
  providedIn: 'root',
})
export class MatchService {
  private apiUrl = 'http://localhost:8080/api/match';

  constructor(private http: HttpClient) {}

  getMatchDataByRoomCode(roomCode: string) {
    return this.http.get<MatchDataResponse>(`${this.apiUrl}/?roomCode=${roomCode}`);
  }

  changeToNextRound(matchId: number) {
    return this.http.patch(`${this.apiUrl}/${matchId}/nextRound`, {});
  }

  endMatch(matchId: number) {
    return this.http.delete(`${this.apiUrl}/${matchId}/end?userId=${sessionStorage.getItem('userId')}`, {});
  }

  getMatchDataByMatchId(matchId: number) {
    return this.http.get<MatchDataResponse>(`${this.apiUrl}/${matchId}`);
  }

}
