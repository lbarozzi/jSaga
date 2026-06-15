import { Component, inject } from '@angular/core';
import { AsyncPipe, DatePipe, NgClass } from '@angular/common';

import { JsagaApiService } from '../core/jsaga-api.service';

@Component({
  selector: 'app-events-page',
  standalone: true,
  imports: [AsyncPipe, DatePipe, NgClass],
  templateUrl: './events-page.component.html',
  styleUrl: './events-page.component.scss'
})
export class EventsPageComponent {
  private readonly api = inject(JsagaApiService);
  protected readonly events$ = this.api.getEvents();
}
