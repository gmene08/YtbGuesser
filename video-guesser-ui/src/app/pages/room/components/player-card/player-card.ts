import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-player-card',
  imports: [],
  templateUrl: './player-card.html',
  styleUrl: './player-card.css',
  standalone: true,
})
export class PlayerCard {
  playerNickname = input.required<string>();

  playerId = input.required<number>();

  isOwner = input.required<boolean>();

  ownerId = input.required<number>();

  currentUserId = sessionStorage.getItem('userId');

  onKickPlayer = output<number>();

  handleKickPlayer() {
    this.onKickPlayer.emit(this.playerId());
  }
}
