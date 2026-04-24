import { TestBed } from '@angular/core/testing';

import { Round } from './round';

describe('Round', () => {
  let service: Round;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Round);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
