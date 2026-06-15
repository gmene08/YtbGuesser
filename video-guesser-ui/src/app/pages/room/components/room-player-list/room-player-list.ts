import { Component, computed, input, output } from '@angular/core';
import { PlayerCard } from '../player-card/player-card';
import { RoomState } from '../../../../models/room.state';

@Component({
  selector: 'app-room-player-list',
  imports: [PlayerCard],
  templateUrl: './room-player-list.html',
  styleUrl: './room-player-list.css',
  standalone: true,
})
export class RoomPlayerList {
  /*[isOwner] =
    ('isUserOwner()'[ownerId] =
      'room.ownerId'[playerId] =
      'player.id'[playerNickname] =
      'player.nickname'(onKickPlayer) =
        'kickPlayer($event)');*/
  room = input.required<RoomState | null>();
  isOwner = input.required<boolean>();
  currentUserId = input.required<number | null>();

  isDisconnected (playerId: number){
    return this.room()?.playersWhoDisconnected?.includes(playerId);
  }

  onKickPlayer = output<number>();
  onLeaveRoom = output<void>();

  handleKickPlayer(playerId: number) {
    this.onKickPlayer.emit(playerId);
  }

  leaveRoom() {
    this.onLeaveRoom.emit();
  }
}
