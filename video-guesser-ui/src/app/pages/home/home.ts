import { Component, signal, inject, viewChild, effect } from '@angular/core';
import { Login } from './components/login/login';
import { Menu } from './components/menu/menu';
import { Auth } from '../../services/auth';
import { NavBar } from '../../components/nav-bar/nav-bar';
import { RoomService } from '../../services/room';
import { Router } from '@angular/router';
import { Reconnect } from './components/reconnect/reconnect';

@Component({
  selector: 'app-home',
  imports: [Login, Menu, NavBar, Reconnect],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  isUserLoggedIn = signal(false);
  isLoading = signal(true);
  userNickname = signal<string | null>(localStorage.getItem('nickname') || null);

  roomUserIsIn = signal<string | null>(null);

  private roomService = inject(RoomService);
  private router = inject(Router);
  private authService = inject(Auth);

  loginComponent = viewChild(Login);
  menuComponent = viewChild(Menu);
  reconnectComponent = viewChild(Reconnect);

  ngOnInit() {
    this.authService.checkSession().subscribe({
      next: (response) => {
        console.log('Session check response: ', response);
        localStorage.setItem('userId', response.id.toString());
        localStorage.setItem('nickname', response.nickname);

        this.isUserLoggedIn.set(true);
        this.roomUserIsIn.set(response.roomIsIn || null);
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
        this.loginComponent()?.errorMsgComponent()?.setErrorMessage('');
        this.menuComponent()?.errorMsgComponent()?.setErrorMessage('');
      },
      error: (error) => {
        const message = error.error?.message || 'Server error. Try again later.';
        console.error(message, error.error);
        this.loginComponent()?.errorMsgComponent()?.setErrorMessage(message);
      },
    });
  }

  handleJoinRoom(joinRoomCode: string) {
    this.roomService.joinRoom(joinRoomCode).subscribe({
      next: (response) => {
        console.log('Room joined successfully', response);
        this.roomUserIsIn.set(response.code);
        this.router.navigate(['/room', response.code]);
      },
      error: (error) => {
        console.error('Error joining room', error);
        this.menuComponent()?.errorMsgComponent()?.setErrorMessage(error.error?.message || 'Server error. Try again later.');
        this.reconnectComponent()?.errorMsgComponent()?.setErrorMessage(error.error?.message || 'Server error. Try again later.');
      },
    });
  }

  handleCreateRoom() {
    this.roomService.createRoom().subscribe({
      next: (response) => {
        console.log('Room created successfully', response);
        this.roomUserIsIn.set(response.code);
        this.router.navigate(['/room', response.code]);
      },
      error: (error) => {
        console.error('Error creating room', error);
        this.menuComponent()?.errorMsgComponent()?.setErrorMessage(error.error?.message || 'Failed to create room. Try again.');
      },
    });
  }

  // Leave option in reconnect component
  handleLeaveRoom() {
    const code = this.roomUserIsIn();
    if(!code) return;
    this.roomService.leaveRoom(code).subscribe({
      // Go to the home page
      next: (response) => {
        console.log('User left room');
        this.roomUserIsIn.set(null);
      },
      error: (error) => {
        console.error('Error leaving room: ', error);
        this.reconnectComponent()?.errorMsgComponent()?.setErrorMessage(error.error?.message || 'Server error. Try again later.');
      },
    });
  }

  handleReconnect() {
    const roomCode = this.roomUserIsIn();
    if (!roomCode) return;
    this.handleJoinRoom(roomCode);
  }

  protected readonly signal = signal;
}
