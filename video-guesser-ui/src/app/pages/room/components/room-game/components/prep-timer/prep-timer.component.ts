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
  showWaitingForHost = signal<boolean>(false);
  roundStatus = input.required<string | null>();

  onFinish = output<void>();

  constructor() {
    effect(()=>{
      if(this.roundStatus() === RoundStatus.Preparing) this.startCountdown();
      if(this.roundStatus() === RoundStatus.Finished) this.showWaitingForHost.set(false);
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

        setTimeout(() => {
          console.log('Escondendo o GO e dando Play!');
          this.showGo.set(false);
          this.onFinish.emit();
        }, 1000);

        setTimeout(() => {
          this.showWaitingForHost.set(true);
        }, 3000)
      }
    }, 1000)
  }
}
