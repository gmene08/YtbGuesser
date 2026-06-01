import {
  Component,
  computed,
  effect,
  inject,
  input,
  OnDestroy,
  OnInit, output,
  signal,
  ViewChild,
} from '@angular/core';
import { MatchService } from '../../../../services/match';
import { PlayerLeaderboard } from './components/player-leaderboard/player-leaderboard';
import { UserGuessRequest } from '../../../../dtos/round.dto';
import { RoundService } from '../../../../services/round';
import { Video } from './components/video/video';
import { GameWebsocketService } from '../../../../services/websocket/game-websocket';
import { RoundStatus } from '../../../../enums/round-status.enum';
import { RoomState } from '../../../../models/room.state';
import { VideoDetails } from './components/video-details/video-details';
import { ChatBox, LogMessage } from './components/chat-box/chat-box';
import { retry } from 'rxjs';
import { MatchStatus } from '../../../../enums/match-status';
import { Results } from './components/results/results';

@Component({
  selector: 'app-room-game',
  imports: [PlayerLeaderboard, Video, VideoDetails, ChatBox, Results],
  templateUrl: './room-game.html',
  styleUrl: './room-game.css',
  standalone: true,
})
export class RoomGame implements OnInit, OnDestroy {
  currentUserId = Number(localStorage.getItem('userId') ?? -1);
  matchService = inject(MatchService);
  gameService = inject(GameWebsocketService);
  roundService = inject(RoundService);

  @ViewChild(Video) videoPlayer!: Video;

  roomData = input.required<RoomState | null>();
  matchData = computed(() => this.gameService.matchData());
  roundData = computed(() => this.gameService.roundData());
  playersWhoGuessed = computed(() => {
    return this.roundData()?.playersWhoGuessed ?? [];
  });
  isUserOwner = computed(() => this.roomData()?.ownerId === this.currentUserId);
  hasUserGuessedThisRound = computed(() => {
    return this.playersWhoGuessed().includes(this.currentUserId);
  });
  isRoundActive = computed(() => this.roundStatus() === RoundStatus.Guessing);
  videoUrl = computed(() => {
    const round = this.roundData();
    if (!round) return null;
    return round.video.url;
  });
  roundStatus = computed(() => {
    const round = this.roundData();
    if (!round) return null;

    return round.roundStatus;
  });
  kickPlayer = output<number>();
  private previousStatus: RoundStatus | null = null;
  userGuess = signal<number>(0);
  displayGuess = computed(() => {
    const guess = this.userGuess();
    return guess === 0 ? '' : guess.toLocaleString();
  });
  timeLeft = signal<number>(30);
  videoStartTime = signal<number>(0);
  activityLogs = signal<LogMessage[]>([]);

  constructor() {
    effect(() => {
      const currentStatus = this.roundStatus();

      if (currentStatus === this.previousStatus) {
        return;
      }

      if (currentStatus === RoundStatus.Guessing) {
        console.log('Status changed to GUESSING, starting timer...');
        this.userGuess.set(0);
        this.startRoundTimer();
      }

      if (currentStatus === RoundStatus.Finished) {
        console.log('Status changed to FINISHED, stopping timer...');
        this.endRoundTimer();
      }

      this.previousStatus = currentStatus;
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

  handleKickPlayer(playerId: number) {
    this.kickPlayer.emit(playerId);
  }

  loadData(roomCode: string) {
    this.matchService
      .getMatchDataByRoomCode(roomCode)
      .pipe(retry({ count: 3, delay: 500 }))
      .subscribe({
        next: (response) => {
          console.log('Match data loaded: ', response);
          this.gameService.matchData.set(response);

          // connect to websocket
          this.gameService.connectToMatch(
            this.roundData()?.roundId || 0,
            this.matchData()?.matchId || 0,
          );
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
    if (this.currentUserId != this.roomData()?.ownerId) {
      return;
    }

    const roundId = this.roundData()?.roundId;
    if (!roundId) return;

    if (this.roundStatus() !== RoundStatus.Preparing) return;

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
    const secondsLeft = this.calculateTimeLeft();
    if (secondsLeft === undefined) return;

    if (secondsLeft > 0) {
      this.syncVideoStartTime(secondsLeft);
      this.timeLeft.set(secondsLeft);

      const interval = setInterval(() => {
        this.timeLeft.update((v) => v - 1);
        if (this.timeLeft() <= 0) {
          clearInterval(interval);

          if (this.currentUserId === this.roomData()?.ownerId) {
            this.endRound();
          }
        }
      }, 1000);
    } else {
      this.timeLeft.set(0);

      if (this.currentUserId === this.roomData()?.ownerId) {
        this.endRound();
      }
    }
  }

  endRound() {
    const roundId = this.roundData()?.roundId;
    if (!roundId) return;

    if (this.roundStatus() !== RoundStatus.Guessing) return;

    if (this.currentUserId != this.roomData()?.ownerId) {
      return;
    }

    this.roundService
      .changeRoundStatus(roundId, {
        userId: this.currentUserId,
        status: RoundStatus.Finished,
      })
      .subscribe({
        next: (response) => {
          console.log('Round ended: ', response);
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

  syncVideoStartTime(secondsLeft: number) {
    const originalStartTime = this.roundData()?.videoStartsAtSecond;
    if (originalStartTime === undefined || originalStartTime === null) return;

    // calculates
    const roundDuration = 5; // hard coded for now
    const secondsElapsed = roundDuration - secondsLeft;

    this.videoStartTime.set(originalStartTime + secondsElapsed);
  }

  calculateTimeLeft() {
    const endsAtString = this.roundData()?.endsAt;
    if (!endsAtString) {
      return;
    }

    const endsAtTime = new Date(endsAtString).getTime();
    const now = new Date().getTime();

    return Math.ceil((endsAtTime - now) / 1000);
  }

  addLog(text: string, type: 'info' | 'error' | 'success' = 'info') {
    const time = new Date().toLocaleTimeString([], {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
    this.activityLogs.update((logs) => [...logs, { type, text, time }]);
  }

  changeToNextRound() {
    const match = this.matchData();
    if (!match) return;

    this.matchService.changeToNextRound(match.matchId).subscribe({
      next: () => {
        console.log('Next round');
      },
      error: (error) => {
        console.log('Error changing to next round: ', error.error?.message);
      },
    });
  }

  endMatch() {
    const match = this.matchData();
    if (!match) return;

    this.matchService.endMatch(match.matchId).subscribe({
      next: () => {
        console.log('Match ended, back to lobby');
      },
      error: (error) => {
        console.log('Error ending match: ', error.error?.message);
      },
    });
  }

  protected onGuessInput(event: Event) {
    const input = event.target as HTMLInputElement;
    const rawValue = input.value.replace(/\D/g, '');
    this.userGuess.set(rawValue ? parseInt(rawValue, 10) : 0);
  }

  onKeyDown(event: KeyboardEvent) {
    const allowedKeys = [
      'Backspace', 'Delete', 'Tab', 'Escape', 'Enter',
      'ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown', 'Home', 'End'
    ];
    if (allowedKeys.includes(event.key)) {
      return; // Deixa passar
    }

    if ((event.ctrlKey || event.metaKey) && ['a', 'c', 'v', 'x'].includes(event.key.toLowerCase())) {
      return; // Deixa passar
    }

    if (!/^[0-9]$/.test(event.key)) {
      event.preventDefault(); // Corta a digitação pela raiz
    }
  }

  protected readonly RoundStatus = RoundStatus;
  protected readonly MatchStatus = MatchStatus;
}
