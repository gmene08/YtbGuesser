import { Component, computed, inject } from '@angular/core';
import { Auth } from '../../services/auth';
import { Router } from '@angular/router';
import { StompLobbyWebsocket } from '../../services/websocket/stomp/stompLobbyWebSocket';
import { GameWebsocketService } from '../../services/websocket/game-websocket';
import { CoreWebsocket } from '../../services/websocket/core-websocket';
import { LobbyWebsocketService } from '../../services/websocket/lobby-websocket';

@Component({
  selector: 'app-nav-bar',
  imports: [],
  templateUrl: './nav-bar.html',
  styleUrl: './nav-bar.css',
  standalone: true,
})
export class NavBar {
  private lobbyWs = inject(LobbyWebsocketService);
  private gameWs = inject(GameWebsocketService);
  private authService = inject(Auth);
  userNickname = computed(() => {
    return this.authService.currentUser()?.nickname || null;
  });
  userId = computed(() => {
    return this.authService.currentUser()?.id || -1;
  });
  private router = inject(Router);

  logout() {
    this.authService.logout(Number(this.userId())).subscribe({
      next: () => {
        console.log('Logout successful');
        //this.gameWebSocket.disconnect();
        //this.lobbyWebSocket.disconnectFromLobby();
        //this.coreWebSocket.disconnect();
        this.lobbyWs.leaveLobby();
        this.router.navigate(['/']);
      },
      error: (response) => {
        console.error('Logout failed');
      },
    });
  }
}
