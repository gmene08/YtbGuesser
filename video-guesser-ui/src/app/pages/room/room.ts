import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { RoomService } from '../../services/room';
import { RoomResponse } from '../../services/room';
import { PlayerCard } from '../../components/player-card/player-card';

@Component({
  selector: 'app-room',
  imports: [PlayerCard],
  templateUrl: './room.html',
  styleUrl: './room.css',
})
export class Room implements OnInit {
  private roomService = inject(RoomService);
  private router = inject(ActivatedRoute);
  private rt = inject(Router);

  roomData = signal<RoomResponse | null>(null);

  roomCode = signal<string>('');

  ngOnInit(): void {
    // Get the room code from the URL
    const code = this.router.snapshot.paramMap.get('code');
    if (code) {
      this.roomCode.set(code);
      console.log('Room code: ', code);

      // Get room data from the server
      this.roomService.getRoomByCode(code).subscribe({
        next: (response) => {
          // Sort players so that the owner is always first
          const sortedPlayers = [...response.players].sort(
            (a, b)=>{
              if (a.id === response.ownerId) return -1;
              if (b.id === response.ownerId) return 1;
              return 0;
            }
          )
          this.roomData.set({
            ...response,
            players: sortedPlayers
          });
          console.log('Room data: ', response);
        },
        error: (error) => {
          console.error('Error fetching room data: ', error);
        },
      });
    } else {
      console.error('Room code not found in URL');
    }
  }

  leaveRoom(){
    this.roomService.leaveRoom(this.roomCode()).subscribe({
      // Go to the home page
      next: (response) =>{
        console.log('User left room');
        this.rt.navigate(['/']);
      },
      error: (error) =>{
        console.error('Error leaving room: ', error);
        alert('Error leaving room');
      }
    })
  }
}
