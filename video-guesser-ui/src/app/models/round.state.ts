import { ActiveRoundResponse, EndOfRoundResponse } from '../dtos/round.dto';

export interface RoundState extends ActiveRoundResponse{
  roundResult?: EndOfRoundResponse;
}
