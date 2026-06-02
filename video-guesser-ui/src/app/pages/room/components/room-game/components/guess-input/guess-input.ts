import { Component, computed, input, output, signal } from '@angular/core';

@Component({
  selector: 'app-guess-input',
  standalone: true,
  templateUrl: './guess-input.html',
})
export class GuessInput {
  isRoundActive = input.required<boolean>();
  hasGuessed = input.required<boolean>();
  isOwner = input.required<boolean>();
  roundFinished = input.required<boolean>();

  guess = output<number>();
  nextRound = output<void>();

  private userGuess = signal<number>(0);

  displayGuess = computed(() => {
    const guessValue = this.userGuess();
    return guessValue === 0 ? '' : guessValue.toLocaleString();
  });

  onInput(event: Event) {
    const inputElement = event.target as HTMLInputElement;
    const rawValue = inputElement.value.replace(/\D/g, '');
    this.userGuess.set(rawValue ? parseInt(rawValue, 10) : 0);
  }

  onKeyDown(event: KeyboardEvent) {
    const allowedKeys = [
      'Backspace',
      'Delete',
      'Tab',
      'Escape',
      'Enter',
      'ArrowLeft',
      'ArrowRight',
      'ArrowUp',
      'ArrowDown',
      'Home',
      'End',
    ];
    if (allowedKeys.includes(event.key)) return;

    if ((event.ctrlKey || event.metaKey) && ['a', 'c', 'v', 'x'].includes(event.key.toLowerCase()))
      return;

    if (!/^[0-9]$/.test(event.key)) {
      event.preventDefault();
    }
  }

  submitGuess() {
    this.guess.emit(this.userGuess());
    // Reseta o input apenas se quiser, ou deixa congelado na tela
    // this.userGuess.set(0);
  }

  // Método opcional para o pai resetar o valor quando o round mudar
  resetInput() {
    this.userGuess.set(0);
  }
}
