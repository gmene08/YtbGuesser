import { Component, signal, inject, viewChild } from '@angular/core';
import { Login } from './components/login/login';
import { Menu } from './components/menu/menu';
import { Auth } from '../../services/auth';
import { NavBar } from '../../components/nav-bar/nav-bar';
import { RoomService } from '../../services/room';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  imports: [Login, Menu, NavBar],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  isUserLoggedIn = signal(false);
  isLoading = signal(true);
  userNickname = signal<string | null>(localStorage.getItem('nickname') || null);

  private roomService = inject(RoomService);
  private router = inject(Router);
  private authService = inject(Auth);

  loginComponent = viewChild(Login);
  menuComponent = viewChild(Menu);

  ngOnInit() {
    this.authService.checkSession().subscribe({
      next: (response) => {
        console.log('Session check response: ', response);
        localStorage.setItem('userId', response.id.toString());
        localStorage.setItem('nickname', response.nickname);

        this.isUserLoggedIn.set(true);
        this.isLoading.set(false);
      },
      error: () => {
        console.error('No session found ');
        localStorage.removeItem('userId');
        localStorage.removeItem('nickname');
        this.isLoading.set(false);
      },
    });
  }

  handleLogin(guestNickname: string) {
    this.authService.createGuest(guestNickname).subscribe({
      next: (response) => {
        console.log('Login successful from home page', response);
        this.isUserLoggedIn.set(true);
        this.userNickname.set(localStorage.getItem('nickname') || null);
        this.loginComponent()?.setErrorMessage("");
        this.menuComponent()?.setErrorMessage("");
      },
      error: (error) => {
        const message = error.error?.message || 'Server error. Try again later.';
        console.error(message, error.error);
        this.loginComponent()?.setErrorMessage(message);
      },
    });
  }

  handleJoinRoom(joinRoomCode: string) {

    this.roomService.joinRoom(joinRoomCode).subscribe({
      next: (response) => {
        console.log('Room joined successfully', response);
        this.router.navigate(['/room', response.code]);
      },
      error: (error) => {
        console.error('Error joining room', error);
        this.menuComponent()?.setErrorMessage(
          error.error?.message || 'Server error. Try again later.',
        );
      },
    });
  }

  handleCreateRoom() {

    this.roomService.createRoom().subscribe({
      next: (response) => {
        console.log('Room created successfully', response);
        this.router.navigate(['/room', response.code]);
      },
      error: (error) => {
        console.error('Error creating room', error);
        this.menuComponent()?.setErrorMessage(
          error.error?.message || 'Failed to create room. Try again.',
        );
      },
    });
  }

  protected readonly signal = signal;
}
