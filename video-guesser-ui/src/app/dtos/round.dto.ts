export interface CurrentRoundResponse {
  roundId: number;
  roundNumber: number;
  roundStatus: string;
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
  guess: number;
}

export interface UserGuessResponse {
  userId: number;
  score: number;
}
