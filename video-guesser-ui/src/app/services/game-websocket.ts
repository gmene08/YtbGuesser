import { Injectable, signal } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { UserGuessRequest } from '../dtos/round.dto';

@Injectable({
  providedIn: 'root',
})
export class GameWebsocketService {
  private client: Client;

  public playersWhoGuessed = signal<number[]>([]);

  constructor() {
    this.client = new Client({
      brokerURL: 'ws://localhost:8080/ws',
      reconnectDelay: 5000,
      debug: (str) => console.log('[STOMP]', str),
    });
  }

  connect(roundId: number) {
    this.client.onConnect = () => {
      console.log('Connected to WebSocket server');
      this.client.subscribe(`/topic/game/${roundId}/guessed-status`, (message) => {
        console.log('Received message:', message.body);

        const data = JSON.parse(message.body);
        if (data.hasGuessed) {
          this.playersWhoGuessed.update((prev) => [...prev, data.userId]);
        }
      });
    };
    this.client.activate();
  }

  disconnect() {
    this.client.deactivate();
    this.playersWhoGuessed.set([]);
  }

  sendGuess(roundId: number, userGuess: UserGuessRequest) {
    if (!this.client.connected) {
      console.error('WebSocket client is not connected');
      return;
    }
    this.client.publish({
      destination: `/app/game/${roundId}/guess`,
      body: JSON.stringify(userGuess),
    });
    console.log('Guess sent:', userGuess);
    this.playersWhoGuessed.update((prev) => [...prev, userGuess.userId]);
  }
}
