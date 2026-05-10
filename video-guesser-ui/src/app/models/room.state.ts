import { RoomResponse } from '../dtos/room.dto';
import { PlayerResponse } from '../dtos/player.dto';

export interface Player extends PlayerResponse{}


export interface RoomState extends Omit<RoomResponse, 'players'>{
  players: Player[];
}
