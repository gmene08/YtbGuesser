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

  constructor() {
    effect(()=>{
      if(this.roundStatus() === RoundStatus.Preparing) this.startCountdown();
    })
  }

  startCountdown() {

    this.countdown.set(5);
    this.showGo.set(false);

    const interval = setInterval(() => {
      this.countdown.update(count => count - 1);
      if (this.countdown() === 0) {
        clearInterval(interval);
        this.showGo.set(true);
      }
    }, 1000)
  }
}
