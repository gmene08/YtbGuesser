import { Component } from '@angular/core';
import { Login } from '../../login/login';

@Component({
  selector: 'app-home',
  imports: [Login],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {

  isUserLoggedIn = false;

  handleLoginSuccess() {
    console.log('Login successful from home page');
    this.isUserLoggedIn = true;
  }
}
