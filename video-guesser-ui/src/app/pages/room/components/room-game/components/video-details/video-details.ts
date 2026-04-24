import { Component, input } from '@angular/core';

@Component({
  selector: 'app-video-details',
  imports: [],
  templateUrl: './video-details.html',
  styleUrl: './video-details.css',
})
export class VideoDetails {
  videoTitle = input.required<string>();

}
