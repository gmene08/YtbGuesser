import { RoundStatus } from '../enums/round-status.enum';

export interface CurrentRoundResponse {
  roundId: number;
  roundNumber: number;
  roundStatus: RoundStatus;
  playersWhoGuessed: number[];
  video: VideoResponse;
}

export interface VideoResponse {
  url: string;
  channelName: string;
  thumbnail: string;
  title: string;
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
