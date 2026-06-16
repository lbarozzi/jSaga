import { Component, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { JsagaApiService, Product, ProductRequest } from '../core/jsaga-api.service';

type ModalMode = 'create' | 'edit';

const CATEGORIES = ['GASTRONOMIA', 'BEVANDA', 'DOLCE', 'ALTRO'] as const;

@Component({
  selector: 'app-products-page',
  standalone: true,
  imports: [CurrencyPipe, NgClass, FormsModule],
  templateUrl: './products-page.component.html',
  styleUrl: './products-page.component.scss',
})
export class ProductsPageComponent implements OnInit {
  private readonly api = inject(JsagaApiService);

  readonly categories = CATEGORIES;
  readonly products = signal<Product[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly modalOpen = signal(false);
  readonly modalMode = signal<ModalMode>('create');
  readonly editingId = signal<number | null>(null);
  readonly saving = signal(false);
  readonly deleteConfirmId = signal<number | null>(null);

  form: ProductRequest = this.emptyForm();

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.api.getProducts().subscribe({
      next: (data) => { this.products.set(data); this.loading.set(false); },
      error: () => { this.error.set('Errore caricamento prodotti.'); this.loading.set(false); },
    });
  }

  openCreate() {
    this.form = this.emptyForm();
    this.editingId.set(null);
    this.modalMode.set('create');
    this.modalOpen.set(true);
  }

  openEdit(p: Product) {
    this.form = { name: p.name, category: p.category, price: p.price, active: p.active };
    this.editingId.set(p.id);
    this.modalMode.set('edit');
    this.modalOpen.set(true);
  }

  save() {
    this.saving.set(true);
    const id = this.editingId();
    const obs = id !== null
      ? this.api.updateProduct(id, this.form)
      : this.api.createProduct(this.form);

    obs.subscribe({
      next: () => { this.modalOpen.set(false); this.saving.set(false); this.load(); },
      error: () => { this.saving.set(false); },
    });
  }

  toggleActive(p: Product) {
    this.api.setProductActive(p.id, !p.active).subscribe(() => this.load());
  }

  confirmDelete(id: number) {
    this.deleteConfirmId.set(id);
  }

  cancelDelete() {
    this.deleteConfirmId.set(null);
  }

  doDelete(id: number) {
    this.api.deleteProduct(id).subscribe(() => {
      this.deleteConfirmId.set(null);
      this.load();
    });
  }

  categoryLabel(cat: string): string {
    const map: Record<string, string> = {
      GASTRONOMIA: 'Gastronomia', BEVANDA: 'Bevanda', DOLCE: 'Dolce', ALTRO: 'Altro',
    };
    return map[cat] ?? cat;
  }

  private emptyForm(): ProductRequest {
    return { name: '', category: 'GASTRONOMIA', price: 0, active: true };
  }
}
