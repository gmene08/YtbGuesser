import { inject, Injectable, signal } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { UserGuessRequest } from '../../dtos/round.dto';
import { CoreWebsocket } from './core-websocket';

@Injectable({
  providedIn: 'root',
})
export class GameWebsocketService {
  private core = inject(CoreWebsocket);

  // load the players who guessed in the round
  public playersWhoGuessed = signal<number[]>([]);

  connect(roundId: number, onPlayerGuessed?: (userId: number) => void) {
    this.core.connect(); // make sure the connection is established

    // load the players who guessed in the round
    this.core.subscribe(`/topic/game/${roundId}/guessed-status`, (message) => {
      const data = JSON.parse(message.body);
      if(data.hasGuessed){
        onPlayerGuessed?.(data.userId);
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
