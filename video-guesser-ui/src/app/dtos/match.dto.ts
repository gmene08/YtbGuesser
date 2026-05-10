import { ActiveRoundResponse } from './round.dto';
import { PlayerCurrentScoreResponse } from './player.dto';

export interface MatchConfigRequest {
  userId: number;
  categories: string[];
  numberOfRounds: number;
}

export interface MatchDataResponse {
  matchId: number;
  maxRounds: number;
  roundNumber: number;
  status: string;
  currentRound: ActiveRoundResponse;
  playerLeaderboard: PlayerCurrentScoreResponse[];
}




