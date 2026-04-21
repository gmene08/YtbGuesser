import { Component, input, output } from '@angular/core';
import { RoomResponse } from '../../../../dtos/room.dto';
import { PlayerCard } from '../player-card/player-card';

@Component({
  selector: 'app-room-player-list',
  imports: [PlayerCard],
  templateUrl: './room-player-list.html',
  styleUrl: './room-player-list.css',
})
export class RoomPlayerList {
  /*[isOwner] =
    ('isUserOwner()'[ownerId] =
      'room.ownerId'[playerId] =
      'player.id'[playerNickname] =
      'player.nickname'(onKickPlayer) =
        'kickPlayer($event)');*/
  room = input.required<RoomResponse | null>();
  isOwner = input.required<boolean>();

  onKickPlayer = output<number>();
  onLeaveRoom = output<void>();

  handleKickPlayer(playerId: number) {
    this.onKickPlayer.emit(playerId);
  }

  leaveRoom() {
    this.onLeaveRoom.emit();
  }
}
