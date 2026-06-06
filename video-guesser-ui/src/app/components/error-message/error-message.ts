import { Component, signal } from '@angular/core';

@Component({
  selector: 'app-error-message',
  imports: [],
  templateUrl: './error-message.html',
  styleUrl: './error-message.css',
})
export class ErrorMessage {
  errorMessage = signal<string>('');
  private timeoutId: any = null;

  setErrorMessage(message: string) {
    this.errorMessage.set(message);
    clearTimeout(this.timeoutId);
    this.timeoutId = setTimeout(() => {
      this.errorMessage.set('');
    }, 5000);
  }
}
