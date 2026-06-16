import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { JsagaApiService, EventItem, EventRequest } from '../core/jsaga-api.service';

type ModalMode = 'create' | 'edit';

@Component({
  selector: 'app-events-page',
  standalone: true,
  imports: [DatePipe, NgClass, FormsModule],
  templateUrl: './events-page.component.html',
  styleUrl: './events-page.component.scss',
})
export class EventsPageComponent implements OnInit {
  private readonly api = inject(JsagaApiService);

  readonly events = signal<EventItem[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly modalOpen = signal(false);
  readonly modalMode = signal<ModalMode>('create');
  readonly editingId = signal<number | null>(null);
  readonly saving = signal(false);
  readonly deleteConfirmId = signal<number | null>(null);

  form: EventRequest = this.emptyForm();

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.api.getEvents().subscribe({
      next: (data) => { this.events.set(data); this.loading.set(false); },
      error: () => { this.error.set('Errore caricamento eventi.'); this.loading.set(false); },
    });
  }

  openCreate() {
    this.form = this.emptyForm();
    this.editingId.set(null);
    this.modalMode.set('create');
    this.modalOpen.set(true);
  }

  openEdit(e: EventItem) {
    this.form = { name: e.name, eventDate: e.eventDate, status: e.status };
    this.editingId.set(e.id);
    this.modalMode.set('edit');
    this.modalOpen.set(true);
  }

  save() {
    this.saving.set(true);
    const id = this.editingId();
    const obs = id !== null
      ? this.api.updateEvent(id, this.form)
      : this.api.createEvent(this.form);

    obs.subscribe({
      next: () => { this.modalOpen.set(false); this.saving.set(false); this.load(); },
      error: () => { this.saving.set(false); },
    });
  }

  toggleStatus(e: EventItem) {
    const next = e.status === 'APERTO' ? 'CHIUSO' : 'APERTO';
    this.api.setEventStatus(e.id, next).subscribe(() => this.load());
  }

  confirmDelete(id: number) {
    this.deleteConfirmId.set(id);
  }

  cancelDelete() {
    this.deleteConfirmId.set(null);
  }

  doDelete(id: number) {
    this.api.deleteEvent(id).subscribe(() => {
      this.deleteConfirmId.set(null);
      this.load();
    });
  }

  private emptyForm(): EventRequest {
    const today = new Date().toISOString().substring(0, 10);
    return { name: '', eventDate: today, status: 'APERTO' };
  }
}
