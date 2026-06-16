import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';

import { JsagaApiService, EventItem, Product } from '../core/jsaga-api.service';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.scss',
})
export class DashboardPageComponent implements OnInit {
  private readonly api = inject(JsagaApiService);

  readonly events = signal<EventItem[]>([]);
  readonly products = signal<Product[]>([]);
  readonly loading = signal(true);

  readonly openEvents = computed(() => this.events().filter((e) => e.status === 'APERTO'));
  readonly activeProducts = computed(() => this.products().filter((p) => p.active));

  readonly productsByCategory = computed(() => {
    const map: Record<string, number> = {};
    for (const p of this.activeProducts()) {
      map[p.category] = (map[p.category] ?? 0) + 1;
    }
    return Object.entries(map).map(([cat, count]) => ({ cat, count }));
  });

  ngOnInit() {
    let done = 0;
    const finish = () => { if (++done === 2) this.loading.set(false); };

    this.api.getEvents().subscribe({ next: (d) => { this.events.set(d); finish(); }, error: finish });
    this.api.getProducts().subscribe({ next: (d) => { this.products.set(d); finish(); }, error: finish });
  }

  categoryLabel(cat: string): string {
    const map: Record<string, string> = {
      GASTRONOMIA: 'Gastronomia', BEVANDA: 'Bevanda', DOLCE: 'Dolce', ALTRO: 'Altro',
    };
    return map[cat] ?? cat;
  }
}
