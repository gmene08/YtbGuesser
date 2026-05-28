import { Component, signal, inject } from '@angular/core';
import { Login } from './components/login/login';
import { Menu } from './components/menu/menu';
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
  private authService = inject(Auth);

  ngOnInit() {
    this.authService.checkSession().subscribe({
      next: (response) => {
        console.log('Session check response: ', response);
        sessionStorage.setItem('userId', response.id.toString());
        sessionStorage.setItem('nickname', response.nickname);

        this.isUserLoggedIn.set(true);
      },
      error: (error) => {
        console.error('No session found ', error);
        sessionStorage.removeItem('userId');
        sessionStorage.removeItem('nickname');
      }
    })

  }

  handleLoginSuccess() {
    console.log('Login successful from home page');
    this.isUserLoggedIn.set(true);
  }
}
