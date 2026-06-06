import { Component, output, viewChild } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ErrorMessage } from '../../../../components/error-message/error-message';

@Component({
  selector: 'app-reconnect',
  imports: [ReactiveFormsModule, ErrorMessage],
  templateUrl: './reconnect.html',
  styleUrl: './reconnect.css',
})
export class Reconnect {
  errorMessage = '';
  onReconnect = output<void>();
  onLeaveRoom = output<void>();

  errorMsgComponent = viewChild(ErrorMessage);

  reconnect() {
    this.onReconnect.emit();
  }

  leaveRoom() {
    this.onLeaveRoom.emit();
  }
}
