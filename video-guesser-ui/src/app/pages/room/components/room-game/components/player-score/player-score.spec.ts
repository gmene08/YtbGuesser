import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlayerScore } from './player-score';

describe('PlayerScore', () => {
  let component: PlayerScore;
  let fixture: ComponentFixture<PlayerScore>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlayerScore],
    }).compileComponents();

    fixture = TestBed.createComponent(PlayerScore);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
