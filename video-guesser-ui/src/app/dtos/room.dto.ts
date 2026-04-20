export interface PlayerResponse {
  id: number;
  nickname: string;
}
export interface RoomResponse {
  id: number;
  code: string;
  ownerId: number;
  players: PlayerResponse[];
  status: string;
  maxPlayers: number;
}
