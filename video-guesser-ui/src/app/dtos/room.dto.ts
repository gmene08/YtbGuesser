import { PlayerResponse } from './player.dto';

export interface RoomResponse {
  id: number;
  code: string;
  ownerId: number;
  players: PlayerResponse[];
  status: string;
  maxPlayers: number;
}
