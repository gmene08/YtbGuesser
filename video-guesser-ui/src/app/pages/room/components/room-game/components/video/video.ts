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
  @ViewChild('videoContainer') private videoContainer!: ElementRef;

  private el = inject(ElementRef);

  roundStatus = input.required<string | null>();
  videoUrl = input.required<string | null>();
  videoStartTime = input.required<number | null>();

  volume = signal<number>(100);
  isMuted = signal(true);
  playerWidth = signal<number>(0);
  playerHeight = signal<number>(0);

  private ytPlayerInstance: any = null;

  startRoundTimer = output<void>();

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
    if (this.videoContainer?.nativeElement) {
      this.playerWidth.set(this.videoContainer.nativeElement.clientWidth);
      this.playerHeight.set(this.videoContainer.nativeElement.clientHeight);
    }
  }

  startRound() {
    this.startRoundTimer.emit();
  }

  onPlayerReady(event: any) {
    this.ytPlayerInstance = event.target;

    this.ytPlayerInstance.setVolume(this.volume());

    if (this.isMuted()) {
      this.ytPlayerInstance.mute();
    } else {
      this.ytPlayerInstance.unMute();
    }

    if (this.roundStatus() === 'GUESSING') {
     this.ytPlayerInstance.seekTo(this.videoStartTime(), true);
     this.ytPlayerInstance.playVideo();
    }
  }

  onVolumeChange(event: any) {
    const input = event.target as HTMLInputElement;
    const newVolume = Number(input.value);

    this.volume.set(newVolume);

    if (this.ytPlayerInstance) {
      this.ytPlayerInstance.setVolume(newVolume);

      if (newVolume > 0 && this.isMuted()) {
        this.isMuted.set(false);
        this.ytPlayerInstance.unMute();
      }

      else if (newVolume === 0 && !this.isMuted()) {
        this.isMuted.set(true);
        this.ytPlayerInstance.mute();
      }
    }

  }

  playVideo() {
    if (this.isMuted()) {
      this.ytPlayerInstance?.mute() || this.videoPlayer?.mute();
    } else {
      this.ytPlayerInstance?.unMute() || this.videoPlayer?.unMute();
    }
    this.ytPlayerInstance?.playVideo() || this.videoPlayer?.playVideo();
  }

  pauseVideo() {
    this.videoPlayer?.pauseVideo();
  }

  toggleMute() {
    this.isMuted.update((current) => !current);

    if (this.isMuted()) {
      this.videoPlayer?.mute();

    } else {
      this.videoPlayer?.unMute();
      if (this.volume() === 0) {
        this.volume.set(50);
        this.ytPlayerInstance?.setVolume(50);
      }
    }

  }

  protected readonly RoundStatus = RoundStatus;
}
