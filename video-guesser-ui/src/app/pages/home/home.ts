import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Auth } from '../../auth';

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

  constructor(private auth: Auth) {}

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

    this.auth.createGuest(this.guestNickname).subscribe(response => {
      console.log("Guest created successfully", response);
    }, error => {
      console.error("Error creating guest", error);
      alert("Error creating guest");
    });
  }

  loginWithAccount(){
    if (!this.loginNickname || !this.loginPassword) {
      alert('Please enter a nickname and password');
    }

    this.auth.login(this.loginNickname, this.loginPassword).subscribe(response => {
      console.log("Login successful", response);
    }, error => {
      console.error("Error logging in", error);
      alert("Error logging in");
    })
  }
}
