import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RoomPlayerList } from './room-player-list';

describe('RoomPlayerList', () => {
  let component: RoomPlayerList;
  let fixture: ComponentFixture<RoomPlayerList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RoomPlayerList],
    }).compileComponents();

    fixture = TestBed.createComponent(RoomPlayerList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
