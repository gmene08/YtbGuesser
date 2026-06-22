import { Component, effect, ElementRef, input, ViewChild } from '@angular/core';
import { DatePipe } from '@angular/common';

export interface LogMessage {
  message: string;
  type: 'SYSTEM' | 'GUESS' | 'RESULT';
  timestamp: string;
}

@Component({
  selector: 'app-chat-box',
  imports: [DatePipe],
  templateUrl: './chat-box.html',
  styleUrl: './chat-box.css',
})
export class ChatBox {
  logs = input.required<LogMessage[]>();

  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  constructor() {
    effect(() => {
      const currentLogs = this.logs();
      setTimeout(() => this.scrollToBottom(), 10);
    });
  }

  private scrollToBottom() {
    try {
      this.scrollContainer.nativeElement.scrollTop =
        this.scrollContainer.nativeElement.scrollHeight;
    } catch (error) {}
  }
}
