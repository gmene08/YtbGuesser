import { Component, input, output, signal, ViewChild } from '@angular/core';
import { YouTubePlayer } from '@angular/youtube-player';
import { Timer } from '../timer/timer';

@Component({
  selector: 'app-video',
  imports: [YouTubePlayer, Timer],
  templateUrl: './video.html',
  styleUrl: './video.css',
})
export class Video {
  @ViewChild(YouTubePlayer) private videoPlayer!: YouTubePlayer;

  videoUrl = input.required<string | undefined>();
  startRoundTimer = output<void>();
  isMuted = signal(true);

  playVideo() {
    this.videoPlayer?.playVideo();
  }
  pauseVideo() {
    this.videoPlayer?.pauseVideo();
  }

  startRound() {
    this.videoPlayer?.playVideo();
    this.startRoundTimer.emit();
  }

  toggleMute() {
    if(this.isMuted()){
      this.videoPlayer?.unMute();
    }else{
      this.videoPlayer?.mute();
    }
    this.isMuted.update(
      (current) => !current
    )
  }
}
