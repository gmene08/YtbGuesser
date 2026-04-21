export interface MatchConfigRequest {
  userId: number;
  categories: string[];
  numberOfRounds: number;
}

export interface MatchDataResponse {
  maxRounds: number;
  roundNumber: number;
  status: string;
  currentRound: CurrentRoundResponse;

}

export interface CurrentRoundResponse {
  roundNumber: number;
  roundStatus: string;
  video: VideoResponse;

}

export interface VideoResponse {
  url: string
  channelName: string
  thumbnail: string
  title: string
  viewCount: number
}
