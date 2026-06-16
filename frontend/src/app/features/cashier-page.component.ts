import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe, DatePipe, NgClass } from '@angular/common';

import { JsagaApiService, Product } from '../core/jsaga-api.service';

export interface CartLine {
  product: Product;
  qty: number;
}

const CATEGORIES = ['TUTTI', 'GASTRONOMIA', 'BEVANDA', 'DOLCE', 'ALTRO'] as const;
type CategoryFilter = (typeof CATEGORIES)[number];

@Component({
  selector: 'app-cashier-page',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, NgClass],
  templateUrl: './cashier-page.component.html',
  styleUrl: './cashier-page.component.scss',
})
export class CashierPageComponent implements OnInit {
  private readonly api = inject(JsagaApiService);

  readonly categories = CATEGORIES;

  readonly products = signal<Product[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly activeCategory = signal<CategoryFilter>('TUTTI');
  readonly cart = signal<CartLine[]>([]);
  readonly showReceipt = signal(false);
  readonly receiptTime = signal<Date | null>(null);
  readonly receiptNumber = signal(0);

  readonly filteredProducts = computed(() => {
    const cat = this.activeCategory();
    return this.products().filter((p) => p.active && (cat === 'TUTTI' || p.category === cat));
  });

  readonly cartTotal = computed(() =>
    this.cart().reduce((sum, l) => sum + l.product.price * l.qty, 0),
  );

  readonly cartCount = computed(() => this.cart().reduce((sum, l) => sum + l.qty, 0));

  ngOnInit() {
    this.api.getProducts(true).subscribe({
      next: (data) => {
        this.products.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Impossibile caricare i prodotti. Verifica la connessione al server.');
        this.loading.set(false);
      },
    });
  }

  addToCart(product: Product) {
    this.cart.update((lines) => {
      const idx = lines.findIndex((l) => l.product.id === product.id);
      if (idx >= 0) {
        const updated = [...lines];
        updated[idx] = { ...updated[idx], qty: updated[idx].qty + 1 };
        return updated;
      }
      return [...lines, { product, qty: 1 }];
    });
  }

  removeOne(productId: number) {
    this.cart.update((lines) => {
      const idx = lines.findIndex((l) => l.product.id === productId);
      if (idx < 0) return lines;
      const updated = [...lines];
      if (updated[idx].qty <= 1) {
        updated.splice(idx, 1);
      } else {
        updated[idx] = { ...updated[idx], qty: updated[idx].qty - 1 };
      }
      return updated;
    });
  }

  removeLine(productId: number) {
    this.cart.update((lines) => lines.filter((l) => l.product.id !== productId));
  }

  confirmPayment() {
    if (this.cart().length === 0) return;
    this.receiptNumber.update((n) => n + 1);
    this.receiptTime.set(new Date());
    this.showReceipt.set(true);
  }

  closeReceipt() {
    this.showReceipt.set(false);
    this.cart.set([]);
  }

  printReceipt() {
    window.print();
  }

  qtyInCart(productId: number): number {
    return this.cart().find((l) => l.product.id === productId)?.qty ?? 0;
  }

  categoryLabel(cat: string): string {
    const map: Record<string, string> = {
      TUTTI: 'Tutti',
      GASTRONOMIA: 'Gastronomia',
      BEVANDA: 'Bevanda',
      DOLCE: 'Dolce',
      ALTRO: 'Altro',
    };
    return map[cat] ?? cat;
  }
}
