import { Component, input, signal } from '@angular/core';

@Component({
  selector: 'app-player-card',
  imports: [],
  templateUrl: './player-card.html',
  styleUrl: './player-card.css',
})
export class PlayerCard {
  playerNickname = input.required<string>();

  playerId = input.required<number>();

  ownerId = input.required<number>();

  currentUserId = sessionStorage.getItem('userId');
}
