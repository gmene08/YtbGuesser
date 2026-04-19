import { Component, input, output, OnInit, signal, inject } from '@angular/core';
import { RoomResponse } from '../../services/room';
import { RoomService } from '../../services/room';


@Component({
  selector: 'app-room-settings',
  imports: [],
  templateUrl: './room-settings.html',
  styleUrl: './room-settings.css',
  standalone: true,
})
export class RoomSettings implements OnInit{
  private roomService = inject(RoomService);

  isOwner = input.required<boolean>();

  room = input<RoomResponse | null>(null);

  closeSettings = output<void>();

  numberOfRounds = signal<number>(5);

  categories = signal<string[]>([]);

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
}
