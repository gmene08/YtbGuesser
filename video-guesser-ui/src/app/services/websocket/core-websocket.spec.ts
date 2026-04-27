import { TestBed } from '@angular/core/testing';

import { CoreWebsocket } from './core-websocket';

describe('CoreWebsocket', () => {
  let service: CoreWebsocket;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CoreWebsocket);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
