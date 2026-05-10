import { MatchDataResponse } from '../dtos/match.dto';
import { RoundState } from './round.state';

export interface MatchState extends Omit<MatchDataResponse, 'currentRound'> {
  currentRound: RoundState;
}
