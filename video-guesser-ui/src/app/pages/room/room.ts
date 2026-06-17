import { Component, computed, effect, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { RoomSettings } from './components/room-settings/room-settings';
import { RoomService } from '../../services/room';
import { MatchConfigRequest } from '../../dtos/match.dto';
import { RoomPlayerList } from './components/room-player-list/room-player-list';
import { NavBar } from '../../components/nav-bar/nav-bar';
import { RoomHeader } from './components/room-header/room-header';
import { RoomGame } from './components/room-game/room-game';
import { StompLobbyWebsocket } from '../../services/websocket/stomp/stompLobbyWebSocket';
import { Player } from '../../models/room.state';
import { retry } from 'rxjs';
import { GameWebsocketService } from '../../services/websocket/game-websocket';
import { CoreWebsocket } from '../../services/websocket/core-websocket';
import { Auth } from '../../services/auth';
import { LobbyWebsocketService } from '../../services/websocket/lobby-websocket';

@Component({
  selector: 'app-room',
  imports: [RoomSettings, RoomPlayerList, NavBar, RoomHeader, RoomGame],
  templateUrl: './room.html',
  styleUrl: './room.css',
  standalone: true,
})
export class Room implements OnInit, OnDestroy {
  private authService = inject(Auth);
  private roomService = inject(RoomService);

  private lobbyWebSocket = inject(StompLobbyWebsocket);
  private gameWebSocket = inject(GameWebsocketService);
  private coreWebSocket = inject(CoreWebsocket);

  private lobbyWs = inject(LobbyWebsocketService);
  private coreWs = inject(CoreWebsocket);
  private gameWs = inject(GameWebsocketService);

  private router = inject(ActivatedRoute);
  private rt = inject(Router);

  currentUserId = computed(() => {
    return this.authService.currentUser()?.id || -1;
  });
  currentUserNickname = computed(() => {
    return this.authService.currentUser()?.nickname || '';
  });

  roomData = computed(() => {
    const room = this.lobbyWs.roomData();
    if (!room) return null;
    return {
      ...room,
      players: this.sortPlayers(room.players, room.ownerId),
    };
  });
  hasLoadedInitialRoomData = computed(() => {
    return !!this.roomData();
  });

  roomCode = signal<string>('');

  startedGame = computed(() => this.roomData()?.status === 'PLAYING');

  startGameErrorMessage = signal<string>('');
  saveMaxPlayersErrorMessage = signal<string>('');

  isUserOwner = computed(() => this.roomData()?.ownerId === this.currentUserId());

  constructor() {
    effect(() => {
      if (!this.hasLoadedInitialRoomData()) return; // guarantees the data is loaded before checking the user's status'

      const room = this.roomData();

      if (!room) return;

      const IsUserStillInRoom =
        room?.players.some((player) => player.userId === this.currentUserId()) ?? false;
      if (!IsUserStillInRoom) {
        console.log('User not in this room');
        //this.lobbyWs.leaveLobby();
        //this.gameWs.leaveGame(room.code);
        this.coreWs.disconnect();
        this.rt.navigate(['/']);
      }
    });
  }

  ngOnDestroy(): void {
    console.log('Room component destroyed. Running safety cleanup...');

    sessionStorage.setItem('justLeft', 'true');
    setTimeout(() => sessionStorage.removeItem('justLeft'), 3000);

    this.coreWs.disconnect();
  }

  ngOnInit(): void {
    // Redirect to home if not logged in
    if (this.currentUserId() === -1) {
      console.log('User not logged in');
      this.rt.navigate(['/']);
      return;
    }

    // Get the room code from the URL
    const code = this.router.snapshot.paramMap.get('code');
    if (code) {
      this.loadRoomData(code);
    } else {
      console.error('Room code not found in URL');
      this.rt.navigate(['/']);
      return;
    }
  }

  loadRoomData(code: string) {
    this.roomCode.set(code);
    console.log('Room code: ', code);

    this.roomService
      .getRoomByCode(code)
      .pipe(retry({ count: 3, delay: 500 }))
      .subscribe({
        next: (response) => {
          const isUserInThisRoom =
            response.players.some((player) => player.userId === this.currentUserId()) ?? false;
          if (!isUserInThisRoom) {
            console.log('User not in this room');
            this.rt.navigate(['/']);
            return;
          }
          this.lobbyWs.roomData.set(response);
          this.lobbyWs.connectToLobby(code);
        },
        error: (error) => {
          console.error('Error fetching room data: ', error);
          this.rt.navigate(['/']);
        },
      });
  }

  leaveRoom() {
    this.roomService.leaveRoom(this.roomCode()).subscribe({
      // Go to the home page
      next: (response) => {
        console.log('User left room');
        //this.gameWebSocket.disconnect();
        //this.lobbyWebSocket.disconnectFromLobby();
        //this.coreWebSocket.disconnect();

        this.coreWs.disconnect();
        this.lobbyWs.roomData.set(null);
        this.rt.navigate(['/']);
      },
      error: (error) => {
        console.error('Error leaving room: ', error);
        this.rt.navigate(['/']);
      },
    });
  }

  kickPlayer(playerId: number) {
    this.roomService.kickPlayer(this.roomCode(), playerId).subscribe({
      next: (response) => {
        /*this.roomData.set({
          ...response,
          players: this.sortPlayers([...response.players], response.ownerId),
        });*/
        console.log('Player kicked');
      },
      error: (error) => {
        console.error('Error kicking player: ', error);
        alert('Error kicking player');
      },
    });
  }

  sortPlayers(players: Player[], currentOwnerId: number) {
    return players.sort((a, b) => {
      if (a.userId === currentOwnerId) return -1;
      if (b.userId === currentOwnerId) return 1;
      return 0;
    });
  }

  startGame(matchConfig: MatchConfigRequest) {
    if (!this.roomCode()) return;

    console.log('Starting game with config: ', matchConfig);
    this.roomService.startRoom(this.roomCode(), matchConfig).subscribe({
      next: (response) => {
        console.log('Game started');
        //this.gameWebSocket.connectToGameEngine(response.code, this.currentUserId(), this.currentUserNickname());
        // code above already is in room-game, match-status is changed through the backend and notified via lobbyUpdate, in which the room-game perceives the change and
        // loads data
      },
      error: (error) => {
        console.error('Error starting game: ', error);
        this.startGameErrorMessage.set(error.error?.message || 'Server error. Try again later.');
      },
    });
  }

  updateMaxPlayers(maxPlayers: number) {
    const room = this.roomData();
    if (!room) return;

    console.log('Max players updated: ', maxPlayers);
    this.roomService.updateRoom(this.roomCode(), { maxPlayers: maxPlayers }).subscribe({
      next: (response) => {
        console.log('Room updated');
        const updatedRoom = { ...room, maxPlayers };
        //this.roomData.set(updatedRoom);
        this.saveMaxPlayersErrorMessage.set('');
      },
      error: (error) => {
        console.error('Error updating room: ', error.error?.message);
        this.saveMaxPlayersErrorMessage.set(
          error.error?.message || 'Server error. Try again later.',
        );
      },
    });
  }
}
