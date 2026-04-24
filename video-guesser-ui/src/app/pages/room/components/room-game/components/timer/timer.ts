import { Component, OnInit, output, signal } from '@angular/core';

@Component({
  selector: 'app-timer',
  imports: [],
  templateUrl: './timer.html',
  styleUrl: './timer.css',
})
export class Timer implements OnInit{

  countdown = signal<number>(3);
  showGo = signal<boolean>(false);

  onFinish = output<void>();

  ngOnInit(){
    this.startCountdown();
  }

  startCountdown() {

    this.countdown.set(3);
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
      }
    }, 1000)
  }
}
