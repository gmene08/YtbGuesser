import { Injectable, inject, signal } from '@angular/core';
import { CoreWebsocket } from './core-websocket';
import { RoomResponse } from '../../dtos/room.dto';
import { RoomState } from '../../models/room.state';

@Injectable({
  providedIn: 'root',
})
export class LobbyWebsocket {
  private core = inject(CoreWebsocket);

  public roomData = signal<RoomState | null>(null);

  private lobbySubscription: any = null;

  connectToLobby(roomCode: string){
    this.core.connect(); // make sure the connection is established

   this.lobbySubscription =  this.core.subscribe(`/topic/room/${roomCode}/lobby`, (message) => {
      const data = JSON.parse(message.body);
      console.log('Received room data: ', data);

      this.roomData.set(data);
    })
  }
  disconnectFromLobby(){
    if(this.lobbySubscription){
      this.lobbySubscription.unsubscribe();
      this.lobbySubscription = null;
    }
    this.roomData.set(null);
  }

  setRoomData(data: RoomResponse){
    this.roomData.set(data);
  }
}
