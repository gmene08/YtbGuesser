import { Component, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-menu',
  imports: [FormsModule],
  templateUrl: './menu.html',
  styleUrl: './menu.css',
})
export class Menu {
  showJoinRoomCodeInput = false;
  joinRoomCode = '';
  private timeoutId: any = null;

  isLoggedIn = input.required<boolean>();

  errorMessage = signal<string>('');

  onJoinRoom = output<string>();
  onCreateRoom = output<void>();

  toggleJoinRoomInput() {
    this.errorMessage.set('');
    this.showJoinRoomCodeInput = !this.showJoinRoomCodeInput;
    if (this.showJoinRoomCodeInput) {
      this.joinRoomCode = '';
    }
  }

  joinRoom() {
    if (!this.isLoggedIn()) {
      this.setErrorMessage('Enter your nickname to join a room');
      return;
    }

    this.onJoinRoom.emit(this.joinRoomCode);
  }

  createRoom() {
    if (!this.isLoggedIn()) {
      this.setErrorMessage('Enter your nickname to create a room');
      return;
    }

    this.onCreateRoom.emit();
  }

  setErrorMessage(message: string) {
    this.errorMessage.set(message);
    clearTimeout(this.timeoutId);
    this.timeoutId = setTimeout(() => {
      this.errorMessage.set('');
    }, 5000);
  }
}
