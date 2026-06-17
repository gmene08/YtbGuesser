import { RoundStatus } from '../enums/round-status.enum';
import { PlayerResultResponse } from './player.dto';

export interface ActiveRoundResponse {
  roundId: number;
  roundNumber: number;
  roundStatus: RoundStatus;
  playersWhoGuessed: number[];
  video: ActiveVideoResponse;
  endsAt: string;
  serverTime: string;
  videoStartsAtSecond: number;

  roundDetails?: EndOfRoundResponse | null;
}

export interface EndOfRoundResponse {
  playersScore: PlayerResultResponse[];
  videoDetails: VideoDetailsResponse;
}

export interface ActiveVideoResponse {
  url: string;
  thumbnail: string;
}

export interface VideoDetailsResponse extends ActiveVideoResponse {
  title: string;
  channelTitle: string;
  viewCount: number;
}

export interface UserGuessRequest {
  userId: number;
  guessedViewCount: number;
}

export interface UpdateRoundStatusRequest {
  userId: number;
  status: RoundStatus;
}
