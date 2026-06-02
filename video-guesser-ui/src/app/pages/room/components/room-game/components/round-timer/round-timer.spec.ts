import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RoundTimer } from './round-timer';

describe('RoundTimer', () => {
  let component: RoundTimer;
  let fixture: ComponentFixture<RoundTimer>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RoundTimer],
    }).compileComponents();

    fixture = TestBed.createComponent(RoundTimer);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
