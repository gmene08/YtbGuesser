import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { RoomSettings } from './components/room-settings/room-settings';
import { RoomService } from '../../services/room';
import { PlayerResponse, RoomResponse } from '../../dtos/room.dto';
import { MatchConfigRequest, MatchDataResponse } from '../../dtos/match.dto';
import { RoomPlayerList } from './components/room-player-list/room-player-list';
import { NavBar } from '../../components/nav-bar/nav-bar';
import { RoomHeader } from './components/room-header/room-header';
import { RoomGame } from './components/room-game/room-game';

@Component({
  selector: 'app-room',
  imports: [RoomSettings, RoomPlayerList, NavBar, RoomHeader, RoomGame],
  templateUrl: './room.html',
  styleUrl: './room.css',
  standalone: true,
})
export class Room implements OnInit {
  private roomService = inject(RoomService);
  private router = inject(ActivatedRoute);
  private rt = inject(Router);

  roomData = signal<RoomResponse | null>(null);

  roomCode = signal<string>('');

  startedGame = computed(() => this.roomData()?.status === 'PLAYING');

  startGameErrorMessage = signal<string>('');
  saveMaxPlayersErrorMessage = signal<string>('');

  isUserOwner = computed(
    () => this.roomData()?.ownerId === Number(sessionStorage.getItem('userId')),
  );

  ngOnInit(): void {
    // Redirect to home if not logged in
    if (sessionStorage.getItem('userId') === null) {
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

    // Get room data from the server
    this.roomService.getRoomByCode(code).subscribe({
      next: (response) => {
        const room = {
          ...response,
          players: this.sortPlayers([...response.players], response.ownerId), // Sort players by ownerId - Owner is always first
        };
        // Set room data in the signal
        this.roomData.set(room);

        // Redirect to home if the user is not in the room
        if (
          !room.players.some((player) => player.id.toString() === sessionStorage.getItem('userId'))
        ) {
          this.rt.navigate(['/']);
          return;
        }

        console.log('Room data: ', response);
      },
      error: (error) => {
        console.error('Error fetching room data: ', error);
        this.rt.navigate(['/']);
        return;
      },
    });
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
        this.roomData.set({
          ...response,
          players: this.sortPlayers([...response.players], response.ownerId),
        });
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
        this.roomData.set(response);
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
        this.roomData.set(updatedRoom);
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
