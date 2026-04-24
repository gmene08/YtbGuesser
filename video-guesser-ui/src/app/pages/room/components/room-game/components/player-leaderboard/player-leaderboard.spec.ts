import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlayerLeaderboard } from './player-leaderboard';

describe('PlayerLeaderboard', () => {
  let component: PlayerLeaderboard;
  let fixture: ComponentFixture<PlayerLeaderboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlayerLeaderboard],
    }).compileComponents();

    fixture = TestBed.createComponent(PlayerLeaderboard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
