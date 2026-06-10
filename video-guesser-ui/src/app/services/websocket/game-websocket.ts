import { Injectable, signal } from '@angular/core';
import { io, Socket } from 'socket.io-client';
import { RoundStatus } from '../../enums/round-status.enum';
import { MatchState } from '../../models/match.state';
import { EndOfRoundResponse } from '../../dtos/round.dto';

@Injectable({
  providedIn: 'root',
})
export class GameWebsocketService {
  private socket!: Socket;

  timeLeft = signal<number>(0);
  roundStatus = signal<RoundStatus | null>(RoundStatus.Preparing);
  playersWhoGuessed = signal<number[]>([]);
  latestMatchData = signal<MatchState | null>(null);
  latestRoundResult = signal<EndOfRoundResponse | null>(null);

  connectToGameEngine(roomCode: string, userId: number, nickname: string) {
    this.socket = io('http://localhost:3000');

    this.socket.on('connect', () => {
      console.log('⚡ Conected to Enginge Node.js! ID:', this.socket.id);
      this.socket.emit('joinGameRoom', { roomCode, userId, nickname });
    });


    this.socket.on('roundStarted', (data) => {
      console.log('▶️ Round started with time:', data.totalTime);
      this.latestRoundResult.set(null);
      this.roundStatus.set(RoundStatus.Guessing);
      this.timeLeft.set(data.totalTime);

    });

    this.socket.on('timeUpdate', (data) => {
      this.timeLeft.set(data.timeLeft);
    });

    this.socket.on('playerGuessed', (data) => {
      this.playersWhoGuessed.update((list) => [...list, data]);
    });

    this.socket.on('roundEnded', (data) => {
      console.log("Round's over", data.reason);
      this.roundStatus.set(RoundStatus.Finished);
    });

    this.socket.on('roundResults', (data) => {
      console.log('🏆 Round results received:', data);

      if (data.matchData) {
        this.latestMatchData.set(data.matchData);
      }

      if (data.roundResult) {
        this.latestRoundResult.set(data.roundResult);
      }
    });

    this.socket.on('changeOfRounds', (data) => {
      console.log('🔄 Mudando para o próximo round!', data);

      this.latestMatchData.set(data);

      this.latestRoundResult.set(null);

      this.roundStatus.set(RoundStatus.Preparing);

      this.playersWhoGuessed.set([]);
    });

    this.socket.on('gameEnded', (data) => {

    })

    this.socket.on('disconnect', () => {
      console.warn('❌ Disconnected from Engine Node.js');
    });
  }

  sendGuess(roomCode: string, userId: number, guessValue: number) {
    if (this.socket) {
      this.socket.emit('submitGuess', { roomCode, userId, guessValue });
      console.log(`🚀 Guess of ${guessValue} sent!`);
    }
  }

  disconnect() {
    if (this.socket) {
      this.socket.disconnect();
    }
  }
}
