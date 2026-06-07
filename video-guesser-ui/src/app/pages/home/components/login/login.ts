import { Component, input, output, signal, viewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ErrorMessage } from '../../../../components/error-message/error-message';

@Component({
  selector: 'app-login',
  imports: [FormsModule, ErrorMessage],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  guestNicknameInput: string = '';
  private timeoutId: any = null;

  isUserLoggedIn = input.required<boolean>();
  userNickname = input.required<string | null>();

  errorMsgComponent = viewChild(ErrorMessage);

  onLogin = output<string>();

  playAsGuest() {
    if (!this.guestNicknameInput) {
      this.errorMsgComponent()?.setErrorMessage('Please enter a nickname');
      return;
    }
    if (this.guestNicknameInput.length < 3 || this.guestNicknameInput.length > 16) {
      this.errorMsgComponent()?.setErrorMessage('Nickname must be between 3 and 16 characters');
      return;
    }

    this.onLogin.emit(this.guestNicknameInput);
    this.guestNicknameInput = '';
  }
}
