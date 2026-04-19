import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface PlayerResponse {
  id: number;
  nickname: string;
}
export interface RoomResponse {
  id: number;
  code: string;
  ownerId: number;
  players: PlayerResponse[];
  status: string;
  maxPlayers: number;

}

@Injectable({
  providedIn: 'root',
})
export class RoomService {

  private apiUrl = 'http://localhost:8080/api/room';

  constructor(private http: HttpClient) {}

  createRoom() {
    const ownerId = sessionStorage.getItem('userId');

    if (ownerId === null) throw new Error("User not logged in");

    return this.http.post<RoomResponse>(`${this.apiUrl}/${ownerId}`, {});
  }

  getRoomByCode(roomCode: string){
    return this.http.get<RoomResponse>(`${this.apiUrl}/${roomCode}`);
  }

  joinRoom(roomCode: string){
    return this.http.post<RoomResponse>(`${this.apiUrl}/join/${roomCode}`, {
      userId: sessionStorage.getItem('userId'),
    });
  }

  leaveRoom(roomCode: string){
    return this.http.delete(
      `${this.apiUrl}/leave/${roomCode}?userId=${sessionStorage.getItem('userId')}`,
      {},
    );
  }

  kickPlayer(roomCode: string, targetUserId: number){
    return this.http.delete<RoomResponse>(
      `${this.apiUrl}/${roomCode}/kick/${targetUserId}?userId=${sessionStorage.getItem('userId')}`,
      {},
    );
  }

  getCategories(){
    return this.http.get<string[]>(`${this.apiUrl}/categories`);
  }
}
