import { computed, inject, Injectable, signal } from '@angular/core';
import { RoundStatus } from '../../enums/round-status.enum';
import { MatchState } from '../../models/match.state';
import { EndOfRoundResponse } from '../../dtos/round.dto';
import { CoreWebsocket } from './core-websocket';

@Injectable({
  providedIn: 'root',
})
export class GameWebsocketService {
  private coreWs = inject(CoreWebsocket);

  timeLeft = signal<number>(0);
  latestMatchData = signal<MatchState | null>(null);

  connectToGameEngine(roomCode: string) {
    this.coreWs.connect();
    this.coreWs.send('joinGameRoom', { roomCode });

    this.coreWs.on('guessingStarted', (data) => {
      console.log('▶️ Round started with time:', data.totalTime);

      this.latestMatchData.update((match) => {
        if (!match) return match;
        return {
          ...match,
          currentRound: {
            ...match.currentRound,
            roundStatus: data.roundStatus,
            roundDetails: null,
            roundNumber: data.roundNumber,
          },
        };
      });

      this.timeLeft.set(data.totalTime);
    });

    this.coreWs.on('timeUpdate', (data) => {
      this.timeLeft.set(data.timeLeft);
    });

    this.coreWs.on('playerGuessed', (data: { userId: number }) => {
      this.latestMatchData.update((match) => {
        if (!match) return match;
        return {
          ...match,
          currentRound: {
            ...match.currentRound,
            playersWhoGuessed: [...match.currentRound.playersWhoGuessed, data.userId],
          },
        };
      });
    });

    this.coreWs.on('roundEnded', (data) => {
      console.log("Round's over", data.reason);

      this.latestMatchData.update((match) => {
        if (!match) return match;
        return {
          ...match,
          currentRound: {
            ...match.currentRound,
            status: RoundStatus.Finished,
          },
        };
      });
    });

    this.coreWs.on('roundResults', (data) => {
      console.log('🏆 Round results received:', data);

      if (data) {
        this.latestMatchData.set(data); // TODO: reduce unnecessary data transfer later
      }
    });

    this.coreWs.on('changeOfRounds', (data) => {
      console.log('🔄 Mudando para o próximo round!', data);

      this.latestMatchData.set(data);
    });

    this.coreWs.on('gameEnded', (data) => {});

    this.coreWs.on('disconnect', () => {
      console.warn('❌ Disconnected from Engine Node.js');
    });
  }

  sendGuess(roomCode: string, userId: number, guessValue: number) {
    this.coreWs.send('submitGuess', { roomCode, userId, guessValue });
    console.log(`🚀 Guess of ${guessValue} sent!`);
  }

  leaveGameEngine(roomCode: string) {
    this.coreWs.send('leaveGameRoom', { roomCode });

    this.latestMatchData.set(null);

  }

  disconnect() {
    this.coreWs.disconnect();
    this.latestMatchData.set(null);
  }
}
