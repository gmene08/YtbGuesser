export interface CurrentRoundResponse {
  roundId: number;
  roundNumber: number;
  roundStatus: string;
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
