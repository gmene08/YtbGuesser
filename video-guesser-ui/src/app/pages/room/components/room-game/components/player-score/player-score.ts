import { Component, input } from '@angular/core';

@Component({
  selector: 'app-player-score',
  imports: [],
  templateUrl: './player-score.html',
  styleUrl: './player-score.css',
  standalone: true,
})
export class PlayerScore {
  playerNickname = input.required<string>();
}
