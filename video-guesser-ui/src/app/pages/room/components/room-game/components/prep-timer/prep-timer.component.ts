import { Component, OnInit, output, signal, effect, input } from '@angular/core';
import { RoundStatus } from '../../../../../../enums/round-status.enum';

@Component({
  selector: 'app-prep-timer',
  imports: [],
  templateUrl: './prep-timer.component.html',
  styleUrl: './prep-timer.component.css',
})
export class PrepTimer {

  countdown = signal<number>(3);
  showGo = signal<boolean>(false);
  roundStatus = input.required<string | null>();
  timeLeft = input.required<number>();

  constructor() {

  }

}
