export interface PlayerResponse {
  userId: number;
  nickname: string;
}

export interface PlayerCurrentScoreResponse extends PlayerResponse {
  totalScore: number;
}

export interface PlayerResultResponse extends PlayerResponse{
  pointsScored: number;
}
