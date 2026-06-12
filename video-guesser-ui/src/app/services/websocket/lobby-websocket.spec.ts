import { TestBed } from '@angular/core/testing';

import { LobbyWebsocketService } from './lobby-websocket';

describe('LobbyWebsocket', () => {
  let service: LobbyWebsocketService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LobbyWebsocketService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
