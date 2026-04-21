import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RoomResponse } from '../dtos/room.dto';
import { MatchConfigRequest, MatchDataResponse } from '../dtos/match.dto';



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

  startRoom(roomCode: string, matchConfig: MatchConfigRequest){
    console.log('Starting game with config: ', matchConfig);
    return this.http.patch<RoomResponse>(`${this.apiUrl}/${roomCode}/start`, matchConfig);
  }

  updateRoom(roomCode: string, roomConfig: Partial<RoomResponse>){
    console.log('Updating room with config: ', roomConfig);
    return this.http.patch(`${this.apiUrl}/${roomCode}`, roomConfig);
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
