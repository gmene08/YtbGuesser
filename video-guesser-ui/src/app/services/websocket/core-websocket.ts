import { Injectable } from '@angular/core';
import { Client } from '@stomp/stompjs';

@Injectable({
  providedIn: 'root',
})
export class CoreWebsocket {
  private client: Client;
  private isConnected = false;

  private connectionQueue: (() => void)[] = [];

  constructor() {
    this.client = new Client({
      brokerURL: '/ws',
      reconnectDelay: 5000,
      debug: (str) => console.log('[STOMP Core]', str),

      onConnect: () => {
        console.log('Connected to WebSocket server');
        this.isConnected = true;

        this.connectionQueue.forEach(callback => callback());
        this.connectionQueue = [];
      },

      onDisconnect: () => {
        this.isConnected = false;
        console.log('Disconnected from WebSocket server');
      }
    });
  }

  public connect() {
    if(!this.client.active)
      this.client.activate();
  }

  public disconnect() {
    if(this.client.active)
      this.client.deactivate();
  }

  public subscribe(destination: string, callback: (message: any) => void) {
    // variable to store the subscription
    let stompSubscription: any = null;

    const action = ()=>{
      stompSubscription = this.client.subscribe(destination, callback);
      console.log(`[STOMP Core] Subscribed to ${destination}`);
    };

    // if it is already connected, execute the action immediately. Otherwise, queue it up.
    if (this.isConnected) {
      action();
    } else {
      this.connectionQueue.push(action);
    }

    // return an unsubscribe function
    return {
      unsubscribe: () => {

        // if the subscription is already active, unsubscribe. Otherwise, remove it from the queue.
        if(stompSubscription){
          stompSubscription.unsubscribe();
          console.log(`[STOMP Core] Unsubscribed from ${destination}`);
        }
        else {
          const index = this.connectionQueue.indexOf(action);
          if (index > -1) {
            this.connectionQueue.splice(index, 1);
            console.log(`[STOMP Core] Removed queued subscription for ${destination} (was not connected yet)`);
          }
        }

      }
    }
  }

  public publish(destination: string, body: any) {
    if (!this.isConnected) {
      console.error('[STOMP Core] WebSocket is not connected');
      return;
    }
    this.client.publish({ destination: destination, body: JSON.stringify(body) });
    console.log(`[STOMP Core] Published to ${destination}:`, body);
  }

}
