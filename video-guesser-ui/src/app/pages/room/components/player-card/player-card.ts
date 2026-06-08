import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-player-card',
  imports: [],
  templateUrl: './player-card.html',
  styleUrl: './player-card.css',
  standalone: true,
  host: {
    class: 'block w-full',
  },
})
export class PlayerCard {
  playerNickname = input.required<string>();
  playerId = input.required<number>();
  isOwner = input.required<boolean | null>();
  ownerId = input.required<number | undefined>();
  currentUserId = input.required<number | null>();
  isDisconnected = input.required<boolean | undefined | null>();
  onKickPlayer = output<number>();


  handleKickPlayer() {
    this.onKickPlayer.emit(this.playerId());
  }
}
