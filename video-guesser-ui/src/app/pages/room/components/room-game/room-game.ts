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
import { RoundTimer } from './components/round-timer/round-timer';
import { GuessInput } from './components/guess-input/guess-input';
import { Auth } from '../../../../services/auth';

@Component({
  selector: 'app-room-game',
  imports: [PlayerLeaderboard, Video, VideoDetails, ChatBox, Results, RoundTimer, GuessInput],
  templateUrl: './room-game.html',
  styleUrl: './room-game.css',
  standalone: true,
})
export class RoomGame implements OnInit, OnDestroy {
  matchService = inject(MatchService);
  gameService = inject(GameWebsocketService);
  roundService = inject(RoundService);
  authService = inject(Auth);
  currentUserId = computed(()=>{
    return this.authService.currentUser()?.id || -1;
  })

  @ViewChild(Video) videoPlayer!: Video;

  roomData = input.required<RoomState | null>();
  matchData = computed(() => this.gameService.matchData());
  roundData = computed(() => this.gameService.roundData());
  playersWhoGuessed = computed(() => {
    return this.roundData()?.playersWhoGuessed ?? [];
  });
  isUserOwner = computed(() => this.roomData()?.ownerId === this.currentUserId());
  hasUserGuessedThisRound = computed(() => {
    return this.playersWhoGuessed().includes(this.currentUserId());
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
  onLeaveRoom = output<void>();
  private previousStatus: RoundStatus | null = null;
  videoStartTime = signal<number>(0);
  activityLogs = signal<LogMessage[]>([]);

  constructor() {
    effect(() => {
      const currentStatus = this.roundStatus();

      if (currentStatus === this.previousStatus) {
        return;
      }

      if (currentStatus === RoundStatus.Finished) {
        this.videoPlayer?.pauseVideo();
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

  submitGuessToBackend(guessedValue: number) {
    const matchData = this.matchData();
    if (!matchData) return;

    if (guessedValue <= 0) {
      alert('Please enter a valid number');
      return;
    }

    console.log('Guessing: ', guessedValue);

    const userGuessRequest: UserGuessRequest = {
      userId: this.currentUserId(),
      guessedViewCount: guessedValue,
    };

    this.gameService.sendGuess(matchData.currentRound.roundId, userGuessRequest);
  }

  startRound() {
    // Using the ownerId so only one http method is called to start a round
    if (!this.isUserOwner()) {
      return;
    }

    const roundId = this.roundData()?.roundId;
    if (!roundId) return;

    if (this.roundStatus() !== RoundStatus.Preparing) return;

    this.roundService
      .changeRoundStatus(roundId, {
        userId: this.currentUserId(),
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

  endRound() {
    const roundId = this.roundData()?.roundId;
    if (!roundId) return;

    if (this.roundStatus() !== RoundStatus.Guessing) return;

    if (!this.isUserOwner()) {
      return;
    }

    this.roundService
      .changeRoundStatus(roundId, {
        userId: this.currentUserId(),
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

  handleTimeIsUp() {
    if (this.isUserOwner()) {
      this.endRound();
    }
  }

  handleLeaveRoom() {
    if (confirm('Are you sure you want to leave the room?')) {
      this.onLeaveRoom.emit();
    }
  }

  protected readonly RoundStatus = RoundStatus;
  protected readonly MatchStatus = MatchStatus;
}
