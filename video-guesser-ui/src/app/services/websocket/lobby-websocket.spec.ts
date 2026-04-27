import { TestBed } from '@angular/core/testing';

import { LobbyWebsocket } from './lobby-websocket';

describe('LobbyWebsocket', () => {
  let service: LobbyWebsocket;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LobbyWebsocket);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
