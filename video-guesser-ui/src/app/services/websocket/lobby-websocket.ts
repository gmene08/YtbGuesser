import { inject, Injectable, signal } from '@angular/core';
import { RoundStatus } from '../../enums/round-status.enum';
import { MatchState } from '../../models/match.state';
import { EndOfRoundResponse } from '../../dtos/round.dto';
import { CoreWebsocket } from './core-websocket';
import { RoomState } from '../../models/room.state';

@Injectable({
  providedIn: 'root',
})
export class LobbyWebsocketService {
  private coreWs = inject(CoreWebsocket);

  public roomData = signal<RoomState | null>(null);

  private isListening = false;

  constructor() {
    this.coreWs.onDisconnect$.subscribe(() => {
      this.isListening = false;
      this.roomData.set(null);
    })
  }

  connectToLobby(roomCode: string) {
    this.coreWs.connect();

    this.coreWs.send('joinLobbyRoom', { roomCode });

    if(this.isListening){
      return;
    }

    this.isListening = true;

    this.coreWs.on('connect', () => {
      console.log('⚡ Connected to Engine Node.js!:');

      //if internet dies
      const currentRoom = this.roomData()?.code;
      if (currentRoom) {
        this.coreWs.send('joinLobbyRoom', { roomCode: currentRoom });
      }
    });

    this.coreWs.on('lobbyUpdate', (data) => {
      console.log('Lobby updated with data: ', data);
      if(data){
        this.roomData.set(data);
      }
    })

  }

}
