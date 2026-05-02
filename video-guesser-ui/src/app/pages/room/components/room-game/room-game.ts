import {
  Component,
  computed,
  effect,
  inject,
  input,
  OnDestroy,
  OnInit,
  signal,
  ViewChild,
} from '@angular/core';
import { RoomResponse } from '../../../../dtos/room.dto';
import { MatchDataResponse } from '../../../../dtos/match.dto';
import { MatchService } from '../../../../services/match';
import { PlayerLeaderboard } from './components/player-leaderboard/player-leaderboard';
import { UserGuessRequest } from '../../../../dtos/round.dto';
import { RoundService } from '../../../../services/round';
import { Video } from './components/video/video';
import { GameWebsocketService } from '../../../../services/websocket/game-websocket';
import { RoundStatus } from '../../../../enums/round-status.enum';

@Component({
  selector: 'app-room-game',
  imports: [PlayerLeaderboard, Video],
  templateUrl: './room-game.html',
  styleUrl: './room-game.css',
  standalone: true,
})
export class RoomGame implements OnInit, OnDestroy {
  currentUserId = Number(sessionStorage.getItem('userId') ?? -1);

  matchService = inject(MatchService);
  gameService = inject(GameWebsocketService);
  roundService = inject(RoundService);

  @ViewChild(Video) videoPlayer!: Video;

  roomData = input.required<RoomResponse | null>();

  matchData = computed(() => this.gameService.matchData());
  roundData = computed(() => this.gameService.roundData());

  playersWhoGuessed = computed(() => {
    return this.roundData()?.playersWhoGuessed ?? [];
  });

  hasUserGuessedThisRound = computed(() => {
    return this.playersWhoGuessed().includes(this.currentUserId);
  });

  isRoundActive = computed(() => this.roundStatus() === 'GUESSING');

  videoUrl = computed(() => {
    return this.roundData()?.video?.url;
  });

  roundStatus = computed(() => {
    return this.roundData()?.roundStatus;
  });

  userGuess = signal<number>(0);

  timeLeft = signal<number>(30);

  constructor() {
    effect(() => {
      /*const roomCode = this.roomData()?.code;
      if (this.roomData()?.status !== 'PLAYING' || !roomCode) {
        return;
      }

      this.loadData(roomCode);*/
      if (this.roundStatus() === 'FINISHED') {
        console.log('Round finished');
        this.endRoundTimer();
      }

      if (this.roundStatus() === 'GUESSING') {
        console.log('Round started');
        this.startRoundTimer();
      }
    });
  }

  ngOnInit() {
    const roomCode = this.roomData()?.code;
    if (this.roomData()?.status !== 'PLAYING' || !roomCode) {
      return;
    }

    this.loadData(roomCode);

    if (document.getElementById('youtube-iframe-api')) return;

    const tag = document.createElement('script');
    tag.id = 'youtube-iframe-api';
    tag.src = 'https://www.youtube.com/iframe_api';
    document.body.appendChild(tag);
  }

  ngOnDestroy() {
    this.gameService.disconnect();
  }

  loadData(roomCode: string) {
    this.matchService.getMatchDataByRoomCode(roomCode).subscribe({
      next: (response) => {
        console.log('Match data loaded: ', response);
        this.gameService.matchData.set(response);

        // connect to websocket
        this.gameService.connect(this.roundData()?.roundId || 0);
      },

      error: (error) => {
        console.error('Error fetching match data: ', error.error?.message || 'Server error');
      },
    });
  }

  guess() {
    const matchData = this.matchData();
    if (!matchData) return;

    if (this.userGuess() <= 0) {
      alert('Please enter a valid number');
      return;
    }

    console.log('Guessing: ', this.userGuess());

    const userGuessRequest: UserGuessRequest = {
      userId: this.currentUserId,
      guessedViewCount: this.userGuess(),
    };

    this.gameService.sendGuess(matchData.currentRound.roundId, userGuessRequest);
  }

  startRound() {
    // Using the ownerId so only one http method is called to start a round
    if (this.currentUserId != this.roomData()?.ownerId) return;

    const roundId = this.roundData()?.roundId;
    if (!roundId) return;

    if (this.roundStatus() !== 'PREPARING') return;

    this.roundService
      .changeRoundStatus(roundId, {
        userId: this.currentUserId,
        status: RoundStatus.Guessing,
      })
      .subscribe({
        next: (response) => {
          console.log('Round started: ', response);
        },
        error: (error) => {
          console.error('Error starting round: ', error.error?.message || 'Server error');
        },
      });
  }

  startRoundTimer() {
    this.timeLeft.set(30);

    const interval = setInterval(() => {
      this.timeLeft.update((v) => v - 1);
      if (this.timeLeft() <= 0) {
        clearInterval(interval);

        if (this.currentUserId === this.roomData()?.ownerId) {
          this.endRound();
        }
      }
    }, 1000);
  }

  endRound() {
    const roundId = this.roundData()?.roundId;
    if (!roundId) return;

    if (this.roundStatus() !== 'GUESSING') return;

    this.roundService
      .changeRoundStatus(roundId, {
        userId: this.currentUserId,
        status: RoundStatus.Finished,
      })
      .subscribe({
        next: (response) => {
          console.log('Round ended: ', response);
          this.endRoundTimer();
        },
        error: (error) => {
          console.error('Error ending round: ', error.error?.message || 'Server error');
        },
      });
  }

  endRoundTimer() {
    this.videoPlayer?.pauseVideo();

    console.log("Time's over");
  }
}

