import { Component, effect, ElementRef, input, ViewChild } from '@angular/core';

export interface LogMessage {
  text: string;
  type: 'info' | 'error' | 'success';
  time: string;
}

@Component({
  selector: 'app-chat-box',
  imports: [],
  templateUrl: './chat-box.html',
  styleUrl: './chat-box.css',
})
export class ChatBox {
  logs = input.required<LogMessage[]>();

  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  constructor() {

    effect(()=>{
      const currentLogs = this.logs();
      setTimeout(()=>
        this.scrollToBottom(),10);

    })
  }

  private scrollToBottom() {
    try {
      this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
    } catch (error) {}
  }

}
