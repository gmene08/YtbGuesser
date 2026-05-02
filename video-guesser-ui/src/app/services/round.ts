import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { UpdateRoundStatusRequest, UserGuessRequest } from '../dtos/round.dto';

@Injectable({
  providedIn: 'root',
})
export class RoundService {
  private apiUrl = 'http://localhost:8080/api/round';

  constructor(private http: HttpClient) {}


  changeRoundStatus(roundId: number, updateRequest: Partial<UpdateRoundStatusRequest>) {
    return this.http.patch(`${this.apiUrl}/${roundId}`, updateRequest);
  }
}
