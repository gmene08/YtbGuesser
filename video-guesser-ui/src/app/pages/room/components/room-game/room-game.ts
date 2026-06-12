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
import { MatchDataResponse } from '../../../../dtos/match.dto';
import { MatchState } from '../../../../models/match.state';

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
  authService = inject(Auth);
  currentUserId = computed(() => {
    return this.authService.currentUser()?.id || -1;
  });
  currentUserNickname = computed(() => {
    return this.authService.currentUser()?.nickname || '';
  });

  @ViewChild(Video) videoPlayer!: Video;

  roomData = input.required<RoomState | null>();

  matchData = computed(()=>{
      return this.gameService.latestMatchData();
    });
  roundData = computed(() => this.matchData()?.currentRound || null);

  timeLeft = computed(() => {
    return this.gameService.timeLeft();
  });

  isUserOwner = computed(() => this.roomData()?.ownerId === this.currentUserId());

  playersWhoGuessed = computed(() => {
    const currentRound = this.matchData()?.currentRound;
    if(!currentRound) return null;
    return currentRound.playersWhoGuessed;
  })
  hasUserGuessedThisRound = computed(() => {
    return this.playersWhoGuessed()?.includes(this.currentUserId());
  });

  roundStatus = computed(() => {
    const currentRound = this.matchData()?.currentRound;
    if (!currentRound) return null;
    return currentRound.roundStatus;
  })

  videoDetails = computed(()=>{
    const roundDetails = this.matchData()?.currentRound.roundDetails;
    if (!roundDetails) return null;
    return roundDetails.videoDetails;
  })

  playersScore = computed(()=>{
    const roundDetails = this.matchData()?.currentRound.roundDetails;
    if (!roundDetails) return null;
    return roundDetails.playersScore;
  })

  isRoundActive = computed(() => this.matchData()?.currentRound.roundStatus === RoundStatus.Guessing);
  videoUrl = computed(() => {
    const round = this.roundData();
    if (!round) return null;
    return round.video.url;
  });
  kickPlayer = output<number>();
  onLeaveRoom = output<void>();
  private previousStatus: RoundStatus | null = null;
  videoStartTime = signal<number>(0);
  activityLogs = signal<LogMessage[]>([]);

  constructor() {

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
          this.gameService.latestMatchData.set(response);

          // sync time if user is reconnecting
          const timeRemaining =
            (new Date(response.currentRound.endsAt).getTime() -
              new Date(response.currentRound.serverTime).getTime()) /
            1000;

          this.gameService.timeLeft.set(Math.round(timeRemaining));
          this.gameService.connectToGameEngine(
            roomCode,
          );
        },

        error: (error) => {
          console.error('Error fetching match data: ', error.error?.message || 'Server error');
        },
      });
  }

  submitGuessToBackend(guessedValue: number) {
    const roomData = this.roomData();
    if (!roomData) return;

    this.gameService.sendGuess(roomData.code, this.currentUserId(), guessedValue);
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

  handleLeaveRoom() {
    if (confirm('Are you sure you want to leave the room?')) {
      this.onLeaveRoom.emit();
    }
  }

  protected readonly RoundStatus = RoundStatus;
  protected readonly MatchStatus = MatchStatus;
}
