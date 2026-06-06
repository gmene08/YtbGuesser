import { Component, input, output, signal, viewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ErrorMessage } from '../../../../components/error-message/error-message';

@Component({
  selector: 'app-menu',
  imports: [FormsModule, ErrorMessage],
  templateUrl: './menu.html',
  styleUrl: './menu.css',
})
export class Menu {
  showJoinRoomCodeInput = false;
  joinRoomCode = '';
  private timeoutId: any = null;

  isLoggedIn = input.required<boolean>();

  errorMsgComponent = viewChild(ErrorMessage);

  onJoinRoom = output<string>();
  onCreateRoom = output<void>();

  toggleJoinRoomInput() {
    this.errorMsgComponent()?.setErrorMessage('');
    this.showJoinRoomCodeInput = !this.showJoinRoomCodeInput;
    if (this.showJoinRoomCodeInput) {
      this.joinRoomCode = '';
    }
  }

  joinRoom() {
    if (!this.isLoggedIn()) {
      this.errorMsgComponent()?.setErrorMessage('Enter your nickname to join a room');
      return;
    }

    this.onJoinRoom.emit(this.joinRoomCode);
  }

  createRoom() {
    if (!this.isLoggedIn()) {
      this.errorMsgComponent()?.setErrorMessage('Enter your nickname to create a room');
      return;
    }

    this.onCreateRoom.emit();
  }
}
