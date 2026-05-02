import { Component, input, output, signal, ViewChild, effect } from '@angular/core';
import { YouTubePlayer } from '@angular/youtube-player';
import { Timer } from '../timer/timer';
import { MatchDataResponse } from '../../../../../../dtos/match.dto';
import { RoundStatus } from '../../../../../../enums/round-status.enum';

@Component({
  selector: 'app-video',
  imports: [YouTubePlayer, Timer],
  templateUrl: './video.html',
  styleUrl: './video.css',
})
export class Video {
  @ViewChild(YouTubePlayer) private videoPlayer!: YouTubePlayer;

  roundStatus = input.required<string | undefined>();
  videoUrl = input.required<string | undefined>();

  isMuted = signal(true);

  startRoundTimer = output<void>();

  constructor() {
    effect(()=>{
      if (this.roundStatus() === 'GUESSING') {
        this.playVideo();
      }
    })
  }

  playVideo() {
    this.videoPlayer?.playVideo();
  }
  pauseVideo() {
    this.videoPlayer?.pauseVideo();
  }

  startRound() {
    this.startRoundTimer.emit();
  }

  toggleMute() {
    if (this.isMuted()) {
      this.videoPlayer?.unMute();
    } else {
      this.videoPlayer?.mute();
    }
    this.isMuted.update((current) => !current);
  }

  protected readonly RoundStatus = RoundStatus;
}
