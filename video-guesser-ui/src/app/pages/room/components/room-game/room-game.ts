import { Component, input, inject, signal, OnInit, effect, computed } from '@angular/core';
import { RoomResponse } from '../../../../dtos/room.dto';
import { MatchDataResponse } from '../../../../dtos/match.dto';
import { MatchService } from '../../../../services/match';
import { PlayerLeaderboard } from './components/player-leaderboard/player-leaderboard';
import { YouTubePlayer } from '@angular/youtube-player';
import { UserGuessRequest } from '../../../../dtos/round.dto';
import { GameService } from '../../../../services/game';

@Component({
  selector: 'app-room-game',
  imports: [PlayerLeaderboard, YouTubePlayer],
  templateUrl: './room-game.html',
  styleUrl: './room-game.css',
  standalone: true,
})
export class RoomGame {
  matchService = inject(MatchService);
  gameService = inject(GameService);

  roomData = input.required<RoomResponse | null>();
  matchData = signal<MatchDataResponse | null>(null);

  userGuess = signal<number>(0);
  hasUserGuessed = signal<boolean>(false);
  userGuessResult = signal<number>(0);

  videoUrl = computed(() => {
    return this.matchData()?.currentRound?.video?.url;
  });

  constructor() {
    effect(() => {
      const roomCode = this.roomData()?.code;
      if (this.roomData()?.status !== 'PLAYING' || !roomCode) {
        return;
      }

      this.loadData(roomCode);
    });
  }

  ngOnInit() {
    if (document.getElementById('youtube-iframe-api')) return;

    const tag = document.createElement('script');
    tag.id = 'youtube-iframe-api';
    tag.src = 'https://www.youtube.com/iframe_api';
    document.body.appendChild(tag);
  }

  public loadData(roomCode: string) {
    this.matchService.getMatchDataByRoomCode(roomCode).subscribe({
      next: (response) => {
        console.log('Match data loaded: ', response);
        this.matchData.set(response);
      },
      error: (error) => {
        console.error('Error fetching match data: ', error.error?.message || 'Server error');
      },
    });
  }

  public guess() {
    const matchData = this.matchData();
    if (!matchData) return;

    if(this.userGuess() <= 0){
      alert('Please enter a valid number');
      return;
    }

    console.log('Guessing: ', this.userGuess());

    const userGuessRequest: UserGuessRequest = {userId: Number(sessionStorage.getItem('userId')), guessedViewCount: this.userGuess()};

    this.gameService.guess(matchData.currentRound.roundId, userGuessRequest).subscribe({
      next: (response) =>{
        this.hasUserGuessed.set(true);
        console.log('User guessed: ', this.userGuess());
        this.userGuessResult.set(response.scoreEarned);
        console.log('User guess result: ', response.scoreEarned);
      },
      error: (error) => {
        console.error('Error guessing: ', error.error?.message || 'Server error');
      }
    });
  }
}
