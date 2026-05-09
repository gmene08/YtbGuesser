import { RoundStatus } from '../enums/round-status.enum';

export interface ActiveRoundResponse {
  roundId: number;
  roundNumber: number;
  roundStatus: RoundStatus;
  playersWhoGuessed: number[];
  video: ActiveVideoResponse;
  endsAt: string;
  videoStartsAtSecond: number;

  roundResult?: RoundResultResponse;
}

export interface RoundResultResponse {
  playersScore: PlayerResultResponse[];
  videoDetails: VideoResultResponse;

}

export interface PlayerResultResponse {
  userId: number;
  pointsScored: number;
}

export interface VideoResultResponse {
  url: string;
  thumbnail: string;
  title: string;
  channelTitle: string;
  viewCount: number;
}

export interface ActiveVideoResponse {
  url: string;
  thumbnail: string;
}

export interface UserGuessRequest {
  userId: number;
  guessedViewCount: number;
}

export interface UpdateRoundStatusRequest {
  userId: number;
  status: RoundStatus;
}
