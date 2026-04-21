import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RoomHeader } from './room-header';

describe('RoomHeader', () => {
  let component: RoomHeader;
  let fixture: ComponentFixture<RoomHeader>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RoomHeader],
    }).compileComponents();

    fixture = TestBed.createComponent(RoomHeader);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
