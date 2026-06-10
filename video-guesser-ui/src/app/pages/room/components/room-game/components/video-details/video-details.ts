import { Component, computed, input } from '@angular/core';
import { RoundStatus } from '../../../../../../enums/round-status.enum';
import { RoundState } from '../../../../../../models/round.state';
import { DecimalPipe } from '@angular/common';
import { VideoDetailsResponse } from '../../../../../../dtos/round.dto';

@Component({
  selector: 'app-video-details',
  imports: [DecimalPipe],
  templateUrl: './video-details.html',
  styleUrl: './video-details.css',
})
export class VideoDetails {
  roundData = input.required<RoundState | null>();
  roundStatus = input.required<RoundStatus | null>();

  videoDetails = input.required <VideoDetailsResponse | null> ();

  protected readonly RoundStatus = RoundStatus;
}
