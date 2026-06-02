import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PrepTimer } from './prep-timer.component';

describe('PrepTimer', () => {
  let component: PrepTimer;
  let fixture: ComponentFixture<PrepTimer>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PrepTimer],
    }).compileComponents();

    fixture = TestBed.createComponent(PrepTimer);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
