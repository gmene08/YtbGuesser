import { Component, computed, effect, inject, input, output } from '@angular/core';
import { RoundStatus } from '../../../../../../enums/round-status.enum';
import { GameWebsocketService } from '../../../../../../services/websocket/game-websocket';

@Component({
  selector: 'app-round-timer',
  standalone: true,
  templateUrl: './round-timer.html',
})
export class RoundTimer {

  // O Angular lê o tempo diretamente do Signal do Node.js, sem fazer nenhuma conta!
  timeLeft = input.required<number>();
  roundStatus = input.required<string | null>();
  isRoundActive = computed(() => this.roundStatus() !== RoundStatus.Preparing );


  protected readonly RoundStatus = RoundStatus;
}
