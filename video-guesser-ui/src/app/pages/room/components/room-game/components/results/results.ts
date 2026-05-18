import { Component, input, output, signal } from '@angular/core';
import { MatchState } from '../../../../../../models/match.state';

@Component({
  selector: 'app-results',
  imports: [],
  templateUrl: './results.html',
  styleUrl: './results.css',
})
export class Results {
  currentUserId = input.required<number | null>();

  isUserOwner = input.required<boolean>();

  matchData = input.required<MatchState | null>();

  backToLobby = output<void>();

  handleBackToLobby() {
    this.backToLobby.emit();
  }
}
