import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface RoomResponse {
  id: number;
  code: string;
  ownerId: number;
  players: {id: number, nickname: string}[];
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
    return this.http.get<RoomResponse>(`http://localhost:8080/api/room/${roomCode}`);
  }

  joinRoom(roomCode: string){
    return this.http.post<RoomResponse>(`http://localhost:8080/api/room/join/${roomCode}`, {userId: sessionStorage.getItem('userId')});
  }

  leaveRoom(roomCode: string){
    return this.http.post<RoomResponse>(`http://localhost:8080/api/room/leave/${roomCode}`, {userId: sessionStorage.getItem('userId')});
  }
}
