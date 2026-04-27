import { Injectable, inject, signal } from '@angular/core';
import { CoreWebsocket } from './core-websocket';
import { RoomResponse } from '../../dtos/room.dto';

@Injectable({
  providedIn: 'root',
})
export class LobbyWebsocket {
  private core = inject(CoreWebsocket);

  public roomData = signal<RoomResponse | null>(null);

  connectToLobby(roomCode: string){
    this.core.connect(); // make sure the connection is established

    this.core.subscribe(`/topic/room/${roomCode}/lobby`, (message) => {
      const data = JSON.parse(message.body);
      console.log('Received room data: ', data);

      this.roomData.set(data);
    })
  }
  disconnectFromLobby(){
    this.core.disconnect();
    this.roomData.set(null);
  }

  setRoomData(data: RoomResponse){
    this.roomData.set(data);
  }
}
