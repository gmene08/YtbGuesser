import { Component, input } from '@angular/core';
import { PlayerCard } from '../../../player-card/player-card';
import { PlayerScore } from '../player-score/player-score';
import { RoomResponse } from '../../../../../../dtos/room.dto';
import { MatchDataResponse } from '../../../../../../dtos/match.dto';
import { RoundStatus } from '../../../../../../enums/round-status.enum';

@Component({
  selector: 'app-player-leaderboard',
  imports: [PlayerScore],
  templateUrl: './player-leaderboard.html',
  styleUrl: './player-leaderboard.css',
  standalone: true,
})
export class PlayerLeaderboard {
  roomData = input.required<RoomResponse | null>();
  playersWhoGuessed = input.required<number[]>();
  matchData = input.required<MatchDataResponse | null>();

  getPlayerRoundScore(userId: number):number{
    const match = this.matchData();

    if(!match?.currentRound?.roundResult?.playersScore){
      return 0;
    }

    const result = match.currentRound.roundResult.playersScore.find(player => player.userId === userId);

    return result?.pointsScored || 0;

  }

  protected readonly RoundStatus = RoundStatus;
}
