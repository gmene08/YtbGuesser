import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-home',
  imports: [FormsModule],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  showGuestInput = false;
  showLoginInput = false;

  guestNickname: string = '';
  loginNickname: string = '';
  loginPassword: string = '';

  toggleGuestInput() {
    this.showGuestInput = !this.showGuestInput;
    this.showLoginInput = false;

    if (this.showGuestInput) {
      this.guestNickname = '';
    }
  }

  toggleLoginInput() {
    this.showLoginInput = !this.showLoginInput;
    this.showGuestInput = false;

    if (this.showLoginInput) {
      this.loginNickname = '';
      this.loginPassword = '';
    }
  }

  playAsGuest() {
    if (!this.guestNickname) {
      alert('Please enter a nickname');
    }
    console.log('play as guest: ' + this.guestNickname);

    // TODO: call backend to play as guest
  }

  loginWithAccount(){
    if (!this.loginNickname || !this.loginPassword) {
      alert('Please enter a nickname and password');
    }
    console.log('play with account: ' + this.loginNickname + ', ' + this.loginPassword);
  }
}
