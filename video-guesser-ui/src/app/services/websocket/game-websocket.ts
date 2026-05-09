import { inject, Injectable, signal, computed } from '@angular/core';
import { ActiveRoundResponse, RoundResultResponse, UserGuessRequest } from '../../dtos/round.dto';
import { CoreWebsocket } from './core-websocket';
import { MatchDataResponse } from '../../dtos/match.dto';

@Injectable({
  providedIn: 'root',
})
export class GameWebsocketService {
  private core = inject(CoreWebsocket);

  matchData = signal<MatchDataResponse | null>(null);
  roundData = computed(()=>{
    return this.matchData()?.currentRound;
  });

  connect(roundId: number, matchId:number) {
    this.core.connect(); // make sure the connection is established

    // load the players who guessed during the round
    this.core.subscribe(`/topic/game/round/${roundId}/guessed-status`, (message) => {
      const data = JSON.parse(message.body);
      if(data.hasGuessed){
        //onPlayerGuessed?.(data.userId);
        this.matchData.update(match => {
          if(!match) return null;

          return {
            ...match,
            currentRound: {
              ...match.currentRound,
              playersWhoGuessed: [...match.currentRound.playersWhoGuessed, data.userId]
            }
          }
        })
      }
    });

    this.core.subscribe(`/topic/game/round/${roundId}/round-status`, (message) => {
      const data = JSON.parse(message.body);
      const roundUpdated = data as ActiveRoundResponse;
      this.matchData.update(match => {
        if(!match) return null;

        return {
          ...match,
          currentRound: {
            ...match.currentRound,
            ...roundUpdated
          }
        };
      });
    });

    this.core.subscribe(`/topic/game/round/${roundId}/round-results`, (message) => {
      const data = JSON.parse(message.body);
      const roundResults = data as RoundResultResponse;
      console.log('Round results: ', roundResults);
      this.matchData.update(match =>{
        if(!match) return null;

        return {
          ...match,
          currentRound:{
            ...match.currentRound,
            roundResult: roundResults
          }
        }
      })

    })

    this.core.subscribe(`/topic/game/match/${matchId}/match-data`, (message) => {
      const data = JSON.parse(message.body);
      const matchData = data as MatchDataResponse;
      this.matchData.set(data);
    })

  }

  sendGuess(roundId: number, userGuess: UserGuessRequest) {
    this.core.publish(`/app/game/${roundId}/guess`, userGuess);
  }

  disconnect() {
    this.core.disconnect();
  }

}
