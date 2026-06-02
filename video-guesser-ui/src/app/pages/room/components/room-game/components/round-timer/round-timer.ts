import { Component, computed, effect, input, OnDestroy, output, signal } from '@angular/core';
import { RoundStatus } from '../../../../../../enums/round-status.enum';


@Component({
  selector: 'app-round-timer',
  standalone: true,
  templateUrl: './round-timer.html',
})
export class RoundTimer implements OnDestroy {
  endsAt = input<string | null | undefined>();
  roundStatus = input.required<RoundStatus | null>();
  videoStartsAtSecond = input<number | null | undefined>();

  timeIsUp = output<void>();
  videoStartTimeUpdated = output<number>();

  timeLeft = signal<number>(30);
  isRoundActive = computed(() => this.roundStatus() === RoundStatus.Guessing);

  private intervalId: any = null;

  constructor() {

    effect(() => {
      const status = this.roundStatus();

      if (status === RoundStatus.Guessing) {
        this.startTimer();
      } else if (status === RoundStatus.Finished) {
        this.stopTimer();
      }
    });
  }

  private startTimer() {
    this.stopTimer();

    const secondsLeft = this.calculateTimeLeft();
    if (secondsLeft === undefined) return;

    if (secondsLeft > 0) {
      this.timeLeft.set(secondsLeft);
      this.syncVideoStartTime(secondsLeft);

      this.intervalId = setInterval(() => {
        this.timeLeft.update((v) => v - 1);

        if (this.timeLeft() <= 0) {
          this.stopTimer();
          this.timeIsUp.emit();
        }
      }, 1000);
    } else {
      this.timeLeft.set(0);
      this.timeIsUp.emit();
    }
  }

  private stopTimer() {
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }
  }

  private calculateTimeLeft() {
    const endsAtString = this.endsAt();
    if (!endsAtString) return;

    const endsAtTime = new Date(endsAtString).getTime();
    const now = new Date().getTime();

    return Math.ceil((endsAtTime - now) / 1000);
  }

  private syncVideoStartTime(secondsLeft: number) {
    const originalStartTime = this.videoStartsAtSecond();
    if (originalStartTime === undefined || originalStartTime === null) return;

    const roundDuration = 30; // TODO: stop the hardcoding !!
    const secondsElapsed = roundDuration - secondsLeft;


    this.videoStartTimeUpdated.emit(originalStartTime + secondsElapsed);
  }

  ngOnDestroy() {
    this.stopTimer();
  }
}
