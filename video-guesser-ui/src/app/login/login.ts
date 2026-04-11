import { Component, EventEmitter, Output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Auth } from '../auth';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})

export class Login {
  showGuestInput = false;
  showLoginInput = false;

  guestNickname: string = '';
  loginNickname: string = '';
  loginPassword: string = '';

  errorMessage= signal<string>('');

  @Output() onLoginSuccess = new EventEmitter<string>();

  constructor(private auth: Auth) {}

  toggleGuestInput() {
    this.errorMessage.set('');
    this.showGuestInput = !this.showGuestInput;
    this.showLoginInput = false;

    if (this.showGuestInput) {
      this.guestNickname = '';
    }
  }

  toggleLoginInput() {
    this.errorMessage.set('');
    this.showLoginInput = !this.showLoginInput;
    this.showGuestInput = false;

    if (this.showLoginInput) {
      this.loginNickname = '';
      this.loginPassword = '';
    }
  }

  playAsGuest() {
    this.errorMessage.set('');

    if (!this.guestNickname) {
      alert('Please enter a nickname');
      return;
    }

    this.auth.createGuest(this.guestNickname).subscribe(
      (response) => {
        console.log('Guest created successfully', response);
        this.onLoginSuccess.emit();
      },
      (error) => {
        const message = error.error?.message || 'Server error. Try again later.';

        console.error(message, error.error);
        this.errorMessage.set(message);
      },
    );
  }

  loginWithAccount() {
    this.errorMessage.set('');

    if (!this.loginNickname || !this.loginPassword) {
      alert('Please enter a nickname and password');
      return;
    }

    this.auth.login(this.loginNickname, this.loginPassword).subscribe(
      (response) => {
        console.log('Login successful', response);
        this.onLoginSuccess.emit();
      },
      (error) => {
        const message = error.error?.message || 'Server error. Try again later.';

        console.error(message, error);
        this.errorMessage.set(message);
      },
    );
  }
}
