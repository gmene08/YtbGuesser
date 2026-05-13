import {
  Component,
  input,
  output,
  signal,
  ViewChild,
  effect,
  computed,
  ElementRef,
  HostListener, inject,
} from '@angular/core';
import { YouTubePlayer } from '@angular/youtube-player';
import { Timer } from '../timer/timer';
import { RoundStatus } from '../../../../../../enums/round-status.enum';

@Component({
  selector: 'app-video',
  imports: [YouTubePlayer, Timer],
  templateUrl: './video.html',
  styleUrl: './video.css',
})
export class Video {
  @ViewChild(YouTubePlayer) private videoPlayer!: YouTubePlayer;
  private el = inject(ElementRef)

  roundStatus = input.required<string | null>();
  videoUrl = input.required<string | null>();
  videoStartTime = input.required<number | null>();

  isMuted = signal(true);
  startRoundTimer = output<void>();

  playerWidth = signal<number>(0);
  playerHeight = signal<number>(0);

  playerVars = computed(() => ({
    autoplay: this.roundStatus() === 'GUESSING' ? 1 : 0,
    mute: 1,
    controls: 0,
    disablekb: 1,
  }));

  constructor() {
    effect(() => {
      if (this.roundStatus() === 'GUESSING' && this.videoPlayer) {
        this.playVideo();
      }
    });
  }

  ngAfterViewInit() {
    setTimeout(() => this.updatePlayerSize(), 0);
  }

  @HostListener('window:resize')
  updatePlayerSize() {
    if (this.el.nativeElement) {
      this.playerWidth.set(this.el.nativeElement.clientWidth);
      this.playerHeight.set(this.el.nativeElement.clientHeight);
    }
  }

  startRound() {
    this.startRoundTimer.emit();
  }

  onPlayerReady(event: any) {
    event.target.mute();

    if (this.roundStatus() === 'GUESSING') {
      event.target.seekTo(this.videoStartTime(), true);
      event.target.playVideo();
    }
  }

  playVideo() {
    if (this.isMuted()) {
      this.videoPlayer?.mute();
    }
    this.videoPlayer?.playVideo();
  }

  pauseVideo() {
    this.videoPlayer?.pauseVideo();
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
