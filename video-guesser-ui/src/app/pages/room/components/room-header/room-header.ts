import { Component, input, signal } from '@angular/core';

@Component({
  selector: 'app-room-header',
  imports: [],
  templateUrl: './room-header.html',
  styleUrl: './room-header.css',
  standalone: true,
})
export class RoomHeader {
  roomCode = input.required<string | undefined>();
  isCodeCopied = signal(false);

  copyCode() {
    const roomCode = this.roomCode();

    if (roomCode) {
      navigator.clipboard
        .writeText(roomCode)
        .then(() => {
          this.isCodeCopied.set(true);
          setTimeout(() => {
            this.isCodeCopied.set(false);
          }, 2000);
        })
        .catch((err) => {
          console.error('Failed to copy room code: ', err);
        });
    }
  }
}
