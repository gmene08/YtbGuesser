import { Injectable } from '@angular/core';
import { io, Socket } from 'socket.io-client';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})

export class CoreWebsocket {

  private socket: Socket | null = null;
  public onDisconnect$ = new Subject<void>();

  connect() {
    if (!this.socket) {
      this.socket = io('http://localhost:3000', {
        withCredentials: true,
      });

      this.socket.on('disconnect', () => {
        this.onDisconnect$.next();
      });

    } else if(this.socket.disconnected) {
      this.socket.connect();
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
    if(this.socket){
      this.socket.emit(event, data);
    }
  }

  on(event: string, callback: (data: any) => void) {
    if (this.socket) {
      this.socket.on(event, callback);
    }
  }
}
