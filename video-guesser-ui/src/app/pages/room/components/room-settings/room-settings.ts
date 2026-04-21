import { Component, input, output, OnInit, signal, inject } from '@angular/core';
import { RoomService } from '../../../../services/room';
import { MatchConfigRequest } from '../../../../dtos/match.dto';
import { RoomResponse } from '../../../../dtos/room.dto';


@Component({
  selector: 'app-room-settings',
  imports: [],
  templateUrl: './room-settings.html',
  styleUrl: './room-settings.css',
  standalone: true,
})
export class RoomSettings implements OnInit {
  private roomService = inject(RoomService);

  saveMaxPlayersErrorMessage = input.required<string>();
  startGameErrorMessage = input.required<string>();
  room = input<RoomResponse | null>(null);
  isOwner = input.required<boolean>();

  onStartGame = output<MatchConfigRequest>();
  onSaveMaxPlayers = output<number>();

  maxPlayers = signal<number>(this.room()?.maxPlayers || 4);
  numberOfRounds = signal<number>(5);
  categories = signal<string[]>([]);

  selectedCategories = signal<string[]>([]);



  ngOnInit() {
    this.roomService.getCategories().subscribe({
      next: (response) => {
        this.categories.set(response);
      },
      error: (error) => {
        console.error('Error fetching categories: ', error);
      },
    });
  }

  toggleCategory(category: string) {
    if (!this.isOwner()) return;

    this.selectedCategories.update((current) => {
      if (current.includes(category)) {
        console.log('Removing category: ', category);
        return current.filter((c) => c !== category);
      } else {
        console.log('Adding category: ', category);
        return [...current, category];
      }
    });
  }

  updateMaxPlayers(event: Event) {
    if (!this.isOwner()) return;
    const target = event.target as HTMLInputElement;
    this.maxPlayers.set(+(target.value));
    console.log('Number of players updated: ', this.maxPlayers());
  }

  saveMaxPlayers() {
    if (!this.isOwner()) return;
    console.log('Saving player count: ', this.maxPlayers());
    this.onSaveMaxPlayers.emit(this.maxPlayers());
  }

  startGame() {
    if (!this.isOwner()) return;
    console.log(
      'Starting game with categories: ',
      this.selectedCategories(),
      ' and rounds: ',
      this.numberOfRounds(),
    );

    // check if valid id
    const rawUserId = sessionStorage.getItem('userId');
    const userId = rawUserId !== null ? Number(rawUserId) : 0;

    let matchConfig: MatchConfigRequest = {
      userId: userId,
      categories: this.selectedCategories(),
      numberOfRounds: this.numberOfRounds(),
    };

    this.onStartGame.emit(matchConfig);
  }
}
