import { Component, computed, input } from '@angular/core';
import { RoundStatus } from '../../../../../../enums/round-status.enum';
import { RoundState } from '../../../../../../models/round.state';
import { DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-video-details',
  imports: [DecimalPipe],
  templateUrl: './video-details.html',
  styleUrl: './video-details.css',
})
export class VideoDetails {
  roundData = input.required<RoundState | null>();
  roundStatus = input.required<RoundStatus | null>();

  videoDetails = computed(() => {
    return this.roundData()?.roundDetails?.videoDetails;
  });

  protected readonly RoundStatus = RoundStatus;
}
