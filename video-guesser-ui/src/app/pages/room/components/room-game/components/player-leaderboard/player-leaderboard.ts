import { Component, input } from '@angular/core';
import { PlayerCard } from '../../../player-card/player-card';
import { PlayerScore } from '../player-score/player-score';
import { RoomResponse } from '../../../../../../dtos/room.dto';

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
}
