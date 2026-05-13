import { Component, input } from '@angular/core';
import { PlayerScore } from '../player-score/player-score';
import { RoundStatus } from '../../../../../../enums/round-status.enum';
import { RoomState } from '../../../../../../models/room.state';
import { MatchState } from '../../../../../../models/match.state';

@Component({
  selector: 'app-player-leaderboard',
  imports: [PlayerScore],
  templateUrl: './player-leaderboard.html',
  styleUrl: './player-leaderboard.css',
  standalone: true,
})
export class PlayerLeaderboard {
  roomData = input.required<RoomState | null>();
  playersWhoGuessed = input.required<number[]>();
  matchData = input.required<MatchState | null>();

  getPlayerRoundScore(userId: number):number{
    const match = this.matchData();

    if(!match?.currentRound?.roundDetails?.playersScore){
      return 0;
    }

    const result = match.currentRound.roundDetails.playersScore.find(player => player.userId === userId);

    return result?.pointsScored || 0;

  }

  protected readonly RoundStatus = RoundStatus;
}
