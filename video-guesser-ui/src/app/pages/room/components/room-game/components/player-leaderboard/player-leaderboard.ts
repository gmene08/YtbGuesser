import { Component, computed, Input, input, output } from '@angular/core';
import { PlayerScore } from '../player-score/player-score';
import { RoundStatus } from '../../../../../../enums/round-status.enum';
import { RoomState } from '../../../../../../models/room.state';
import { MatchState } from '../../../../../../models/match.state';
import { PlayerCard } from '../../../player-card/player-card';
import { RoundState } from '../../../../../../models/round.state';
import { EndOfRoundResponse } from '../../../../../../dtos/round.dto';
import { PlayerResultResponse } from '../../../../../../dtos/player.dto';

@Component({
  selector: 'app-player-leaderboard',
  imports: [ PlayerCard ],
  templateUrl: './player-leaderboard.html',
  styleUrl: './player-leaderboard.css',
  standalone: true,
})
export class PlayerLeaderboard {
  roomData = input.required<RoomState | null>();
  playersWhoGuessed = input.required<number[] | null>();
  matchData = input.required<MatchState | null>();
  roundData = input.required<RoundState | null>();
  playersScore = input.required<PlayerResultResponse[] | null>();
  currentUserId = input.required<number | null>();
  isUserOwner = input.required<boolean>();
  isGuessing = computed(()=>{
    return !!this.roundData()?.roundStatus && this.roundData()?.roundStatus !== RoundStatus.Finished ;
  })

  kickPlayer = output<number>();

  playerHasGuessed (playerId: number){
    const pwg = this.playersWhoGuessed();
    if (!pwg) return false;
    return pwg.includes(playerId);
  }

  getPlayerRoundScore(userId: number):number{

    if (!this.playersScore()) {
      return 0;
    }

    const result = this.playersScore()?.find((player) => player.userId === userId);

    return result?.pointsScored || 0;

  }

  handleKickPlayer(playerId: number) {
    this.kickPlayer.emit(playerId);
  }

  protected readonly RoundStatus = RoundStatus;
}
