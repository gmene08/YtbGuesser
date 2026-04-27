import { Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { RoomSettings } from './components/room-settings/room-settings';
import { RoomService } from '../../services/room';
import { PlayerResponse, RoomResponse } from '../../dtos/room.dto';
import { MatchConfigRequest, MatchDataResponse } from '../../dtos/match.dto';
import { RoomPlayerList } from './components/room-player-list/room-player-list';
import { NavBar } from '../../components/nav-bar/nav-bar';
import { RoomHeader } from './components/room-header/room-header';
import { RoomGame } from './components/room-game/room-game';
import { LobbyWebsocket } from '../../services/websocket/lobby-websocket';

@Component({
  selector: 'app-room',
  imports: [RoomSettings, RoomPlayerList, NavBar, RoomHeader, RoomGame],
  templateUrl: './room.html',
  styleUrl: './room.css',
  standalone: true,
})
export class Room implements OnInit {
  currentUserId = Number(sessionStorage.getItem('userId') ?? -1);

  private roomService = inject(RoomService);
  private lobbyService = inject(LobbyWebsocket);
  private router = inject(ActivatedRoute);
  private rt = inject(Router);

  roomData = computed(() => {
    const room = this.lobbyService.roomData();
    if (!room) return null;
    return {
      ...room,
      players: this.sortPlayers(room.players, room.ownerId),
    };
  });
  hasLoadedInitialRoomData = signal(false);

  roomCode = signal<string>('');

  startedGame = computed(() => this.roomData()?.status === 'PLAYING');

  startGameErrorMessage = signal<string>('');
  saveMaxPlayersErrorMessage = signal<string>('');

  isUserOwner = computed(
    () => this.roomData()?.ownerId === this.currentUserId,
  );

  constructor() {
    effect(() => {
      if (!this.hasLoadedInitialRoomData()) return; // guarantees the data is loaded before checking the user's status'

      const room = this.roomData();

      const IsUserStillInRoom = room?.players.some(player => player.id === this.currentUserId) ?? false;
      if (!IsUserStillInRoom) {
        console.log('User not in this room');
        this.lobbyService.disconnectFromLobby();
        this.rt.navigate(['/']);
      }
    })
  }

  ngOnInit(): void {
    // Redirect to home if not logged in
    if (this.currentUserId === -1) {
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

    this.roomService.getRoomByCode(code).subscribe({
      next: (response) =>{
        const isUserInThisRoom = response.players.some(player => player.id === this.currentUserId) ?? false;
        if (!isUserInThisRoom) {
          console.log('User not in this room');
          this.rt.navigate(['/']);
          return;
        }

        this.lobbyService.setRoomData(response);
        this.hasLoadedInitialRoomData.set(true);
        this.lobbyService.connectToLobby(code);
      },
      error: (error) => {
        console.error('Error fetching room data: ', error);
        this.rt.navigate(['/']);
      },
    })
  }

  leaveRoom() {
    this.roomService.leaveRoom(this.roomCode()).subscribe({
      // Go to the home page
      next: (response) => {
        console.log('User left room');
        this.rt.navigate(['/']);
      },
      error: (error) => {
        console.error('Error leaving room: ', error);
        alert('Error leaving room');
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

  sortPlayers(players: PlayerResponse[], currentOwnerId: number) {
    return players.sort((a, b) => {
      if (a.id === currentOwnerId) return -1;
      if (b.id === currentOwnerId) return 1;
      return 0;
    });
  }

  startGame(matchConfig: MatchConfigRequest) {
    if (!this.roomCode()) return;

    console.log('Starting game with config: ', matchConfig);
    this.roomService.startRoom(this.roomCode(), matchConfig).subscribe({
      next: (response) => {
        console.log('Game started');
        //this.roomData.set(response);
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
