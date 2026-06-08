import { PlayerResponse } from './player.dto';

export interface RoomResponse {
  id: number;
  code: string;
  ownerId: number;
  players: PlayerResponse[];
  playersWhoDisconnected: number[];
  status: string;
  maxPlayers: number;
}
