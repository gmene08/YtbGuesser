import { Component, input, inject, signal, OnInit, effect } from '@angular/core';
import { RoomResponse } from '../../../../dtos/room.dto';
import { MatchDataResponse } from '../../../../dtos/match.dto';
import { MatchService } from '../../../../services/match';

@Component({
  selector: 'app-room-game',
  imports: [],
  templateUrl: './room-game.html',
  styleUrl: './room-game.css',
})
export class RoomGame {
  matchService = inject(MatchService);

  roomData = input.required<RoomResponse | null>();
  matchData = signal<MatchDataResponse | null>(null);

  constructor() {
    effect(()=>{

      const roomCode = this.roomData()?.code;
      if(this.roomData()?.status !== 'PLAYING' || !roomCode){
        return;
      }

      this.loadData(roomCode);
    })
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
}
