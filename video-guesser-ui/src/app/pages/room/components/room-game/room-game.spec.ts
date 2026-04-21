import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RoomGame } from './room-game';

describe('RoomGame', () => {
  let component: RoomGame;
  let fixture: ComponentFixture<RoomGame>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RoomGame],
    }).compileComponents();

    fixture = TestBed.createComponent(RoomGame);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
