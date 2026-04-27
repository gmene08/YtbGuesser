import { inject, Injectable, signal } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { UserGuessRequest } from '../../dtos/round.dto';
import { CoreWebsocket } from './core-websocket';

@Injectable({
  providedIn: 'root',
})
export class GameWebsocketService {
  private core = inject(CoreWebsocket);

  public playersWhoGuessed = signal<number[]>([]);

  connect(roundId: number) {
    this.core.connect(); // make sure the connection is established

    this.core.subscribe(`/topic/game/${roundId}/guessed-status`, (message) => {
      const data = JSON.parse(message.body);
      if(data.hasGuessed){
        this.playersWhoGuessed.update(prev => [...prev, data.userId]);
      }
    });
  }

  sendGuess(roundId: number, userGuess: UserGuessRequest) {
    this.core.publish(`/app/game/${roundId}/guess`, userGuess);
  }

  disconnect() {
    this.core.disconnect();
    this.playersWhoGuessed.set([]);
  }

}
