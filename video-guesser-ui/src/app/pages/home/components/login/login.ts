import { Component, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  guestNickname: string = '';
  private timeoutId: any = null;

  errorMessage = signal<string>('');

  onLogin = output<string>();

  playAsGuest() {
    if (!this.guestNickname) {
      this.setErrorMessage('Please enter a nickname');
      return;
    }
    if (this.guestNickname.length < 3 || this.guestNickname.length > 16) {
      this.setErrorMessage('Nickname must be between 3 and 16 characters');
      return;
    }

    this.onLogin.emit(this.guestNickname);
  }

  setErrorMessage(message: string) {
    this.errorMessage.set(message);
    clearTimeout(this.timeoutId);
    this.timeoutId = setTimeout(() => {
      this.errorMessage.set('');
    }, 5000);
  }
}
