import { Injectable, inject, signal } from '@angular/core';
import { StompCoreWebsocket } from './stompCoreWebSocket';
import { RoomState } from '../../../models/room.state';
import { RoomResponse } from '../../../dtos/room.dto';


@Injectable({
  providedIn: 'root',
})

//VERY IMPORTANT --- THIS CLASS IS DISCONTINUED AND NOT USED FOR ANYTHING SINCE ADDING THE NODE ENGINE!!
//KEEPING IT HERE IN CASE I EVER WANT TO REVISIT IT
export class StompLobbyWebsocket {
  private core = inject(StompCoreWebsocket);

  public roomData = signal<RoomState | null>(null);

  private lobbySubscription: any = null;

  connectToLobby(roomCode: string) {
    this.core.connect(); // make sure the connection is established

    this.lobbySubscription = this.core.subscribe(`/topic/room/${roomCode}/lobby`, (message) => {
      const data = JSON.parse(message.body);
      console.log('Received room data: ', data);

      this.roomData.set(data);
    });
  }
  disconnectFromLobby() {
    if (this.lobbySubscription) {
      this.lobbySubscription.unsubscribe();
      this.lobbySubscription = null;
    }
    this.roomData.set(null);
  }

  setRoomData(data: RoomResponse) {
    this.roomData.set(data);
  }
}
