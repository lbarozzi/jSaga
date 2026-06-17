import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe, DatePipe, NgClass } from '@angular/common';

import { JsagaApiService, OrderResponse, Product } from '../core/jsaga-api.service';

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
  readonly orderLoading = signal(false);
  readonly orderError = signal<string | null>(null);
  readonly lastOrder = signal<OrderResponse | null>(null);
  readonly printLoading = signal(false);
  readonly printStatus = signal<'idle' | 'ok' | 'error'>('idle');

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
    if (this.cart().length === 0 || this.orderLoading()) return;
    this.orderLoading.set(true);
    this.orderError.set(null);

    const req = {
      eventId: null,
      paymentMethod: 'CONTANTI',
      items: this.cart().map((l) => ({ productId: l.product.id, qty: l.qty })),
    };

    this.api.submitOrder(req).subscribe({
      next: (order) => {
        this.lastOrder.set(order);
        //this.receiptNumber.update((n) => n + 1);
        this.receiptNumber.set(order.id);
        //this.receiptTime.set(new Date());
        this.receiptTime.set(order.createdAt ? new Date(order.createdAt) : new Date());
        this.printStatus.set(order.printed ? 'ok' : 'idle');
        this.showReceipt.set(true);
        this.orderLoading.set(false);
      },
      error: () => {
        this.orderError.set('Errore durante il salvataggio dell\'ordine. Riprova.');
        this.orderLoading.set(false);
      },
    });
  }

  closeReceipt() {
    this.showReceipt.set(false);
    this.printStatus.set('idle');
    this.lastOrder.set(null);
    this.cart.set([]);
  }

  printOnPrinter() {
    const order = this.lastOrder();
    if (!order || this.printLoading()) return;
    this.printLoading.set(true);
    this.printStatus.set('idle');

    this.api.printOrder(order.id).subscribe({
      next: () => {
        this.printStatus.set('ok');
        this.printLoading.set(false);
      },
      error: () => {
        this.printStatus.set('error');
        this.printLoading.set(false);
      },
    });
  }
  reprintReceipt() {
    //curl -sS -i -X POST http://localhost:8080/api/v1/orders/3/print
    console.log('Reprinting receipt for order id:', this.receiptNumber());
    this.api.printOrder(this.receiptNumber()).subscribe({
      next: () => {  console.log('Receipt reprinted successfully.');},
      error: () => {console.error('Error reprinting receipt for order id:', this.receiptNumber()); }
    });
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
