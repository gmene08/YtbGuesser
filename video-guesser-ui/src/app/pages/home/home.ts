import { Component, signal } from '@angular/core';
import { Login } from './components/login/login';
import { Menu } from './components/menu/menu';
import { inject } from 'vitest';
import { Auth } from '../../services/auth';
import { NavBar } from '../../components/nav-bar/nav-bar';

@Component({
  selector: 'app-home',
  imports: [Login, Menu, NavBar],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  constructor(private auth: Auth) {}

  isUserLoggedIn = signal(false);

  ngOnInit() {
    const userId = sessionStorage.getItem('userId');

    if (userId && userId !== 'undefined') {
      this.isUserLoggedIn.set(true);
      console.log('User is logged in: ', userId);
    }
  }

  handleLoginSuccess() {
    console.log('Login successful from home page');
    this.isUserLoggedIn.set(true);
  }
}
