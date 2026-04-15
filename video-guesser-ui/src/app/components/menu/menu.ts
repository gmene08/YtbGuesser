import { Component, inject, signal } from '@angular/core';
import { RoomService } from '../../services/room';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-menu',
  imports: [FormsModule],
  templateUrl: './menu.html',
  styleUrl: './menu.css',
})
export class Menu {
  private roomService = inject(RoomService);
  private router = inject(Router);

  userNickname = signal<string | null>(sessionStorage.getItem('nickname') || null);

  showJoinRoomCodeInput = false;
  joinRoomCode = '';

  errorMessage = signal<string>('');

  toggleJoinRoomInput() {
    this.errorMessage.set('');
    this.showJoinRoomCodeInput = !this.showJoinRoomCodeInput;
    if (this.showJoinRoomCodeInput) {
      this.joinRoomCode = '';
    }
  }

  joinRoom() {
    this.roomService.joinRoom(this.joinRoomCode).subscribe(
      (response) => {
        console.log('Room joined successfully', response);
        this.router.navigate(['/room', response.code]);
      },
      (error) => {
        console.error('Error joining room', error);
        this.errorMessage.set(error.error?.message || 'Server error. Try again later.');
      },
    );
  }

  createRoom() {
    this.roomService.createRoom().subscribe(
      (response) => {
        console.log('Room created successfully', response);
        this.router.navigate(['/room', response.code]);
      },
      (error) => {
        console.error('Error creating room', error);
      },
    );
  }
}
