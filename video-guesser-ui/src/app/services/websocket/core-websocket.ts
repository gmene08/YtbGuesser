import { Injectable } from '@angular/core';
import { io, Socket } from 'socket.io-client';
import { Subject } from 'rxjs';


@Injectable({
  providedIn: 'root',
})
export class CoreWebsocket {
  private socket: Socket | null = null;
  public onDisconnect$ = new Subject<void>();
  isLocal = window.location.hostname === 'localhost';

  socketUrl = this.isLocal ? 'http://localhost:3000' : '/';

  connect() {
    if (!this.socket) {
      this.socket = io(this.socketUrl, {
        withCredentials: true,
        transports: ['websocket']
      });

      this.socket.on('disconnect', (reason) => {
        console.warn('Disconnected from server, reason: ', reason);
        this.onDisconnect$.next();
      });

      this.socket.on('reconnect', () => {
        console.log('🔄 Reconnected to server with success');
      });
    }
  }
  disconnect() {
    if (this.socket) {
      this.onDisconnect$.next();

      this.socket.removeAllListeners();
      this.socket.disconnect();
      this.socket = null;
    }
  }
  send(event: string, data: any) {
    if (this.socket) {
      this.socket.emit(event, data);
    }
  }

  on(event: string, callback: (data: any) => void) {
    if (this.socket) {
      this.socket.on(event, callback);
    }
  }
}
