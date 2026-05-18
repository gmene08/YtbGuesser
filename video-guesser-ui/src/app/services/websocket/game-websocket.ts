import { inject, Injectable, signal, computed } from '@angular/core';
import { ActiveRoundResponse, EndOfRoundResponse, UserGuessRequest } from '../../dtos/round.dto';
import { CoreWebsocket } from './core-websocket';
import { MatchDataResponse } from '../../dtos/match.dto';
import { MatchState } from '../../models/match.state';

@Injectable({
  providedIn: 'root',
})
export class GameWebsocketService {
  private core = inject(CoreWebsocket);

  matchData = signal<MatchState | null>(null);
  roundData = computed(()=>{
    const match = this.matchData();
    if(!match) return null;
    return match.currentRound;
  });

  private currentRoundSubscriptions: any[] = [];
  private matchSubscription: any = null;

  connectToMatch(initialRoundId: number, matchId:number) {
    this.core.connect(); // make sure the connection is established

    this.matchSubscription = this.core.subscribe(`/topic/game/match/${matchId}/match-data`, (message) => {
      const data = JSON.parse(message.body);
      const newMatchData = data as MatchDataResponse;

      // keep track of the old round ID
      const oldRoundId = this.matchData()?.currentRound?.roundId;

      this.matchData.set(newMatchData);

      // If the round ID has changed, subscribe to the new round
      if (oldRoundId && oldRoundId !== newMatchData?.currentRound?.roundId) {
        console.log('Round changed, unsubscribing from old round');
        this.subscribeToRound(newMatchData.currentRound.roundId);
      }
    });

    // First time connecting, so subscribe to the initial round
    this.subscribeToRound(initialRoundId);

  }

  subscribeToRound(roundId: number) {
    // Unsubscribe from any previous subscriptions
    this.currentRoundSubscriptions.forEach(sub => sub.unsubscribe());
    this.currentRoundSubscriptions = [];

    console.log('Subscribing to round: ', roundId);

    const subGuess = this.core.subscribe(`/topic/game/round/${roundId}/guessed-status`, (message) => {
      const data = JSON.parse(message.body);
      if (data.hasGuessed) {
        //onPlayerGuessed?.(data.userId);
        this.matchData.update((match) => {
          if (!match) return null;
          return {
            ...match,
            currentRound: {
              ...match.currentRound,
              playersWhoGuessed: [...match.currentRound.playersWhoGuessed, data.userId],
            },
          };
        });
      }
    });

    const subStatus = this.core.subscribe(
      `/topic/game/round/${roundId}/round-status`,
      (message) => {
        const data = JSON.parse(message.body);
        const roundUpdated = data as ActiveRoundResponse;
        this.matchData.update((match) => {
          if (!match) return null;

          return {
            ...match,
            currentRound: {
              ...match.currentRound,
              ...roundUpdated,
            },
          };
        });
      },
    );

    const subResults = this.core.subscribe(
      `/topic/game/round/${roundId}/round-results`,
      (message) => {
        const data = JSON.parse(message.body);
        const roundResults = data as EndOfRoundResponse;
        console.log('Round results: ', roundResults);
        this.matchData.update((match) => {
          if (!match) return null;

          return {
            ...match,
            currentRound: {
              ...match.currentRound,
              roundResult: roundResults,
            },
          };
        });
      },
    );

    this.currentRoundSubscriptions.push(subGuess, subStatus, subResults);
  }

  sendGuess(roundId: number, userGuess: UserGuessRequest) {
    this.core.publish(`/app/game/${roundId}/guess`, userGuess);
  }

  disconnect() {
    this.currentRoundSubscriptions.forEach(sub => sub.unsubscribe());
    this.currentRoundSubscriptions = [];

    if(this.matchSubscription){
      this.matchSubscription.unsubscribe();
      this.matchSubscription = null;
    }

    this.matchData.set(null);

  }

}
