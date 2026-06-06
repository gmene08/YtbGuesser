import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Reconnect } from './reconnect';

describe('Reconnect', () => {
  let component: Reconnect;
  let fixture: ComponentFixture<Reconnect>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Reconnect],
    }).compileComponents();

    fixture = TestBed.createComponent(Reconnect);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
