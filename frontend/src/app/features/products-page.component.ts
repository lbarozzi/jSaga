import { Component, inject } from '@angular/core';
import { AsyncPipe, CurrencyPipe, NgClass } from '@angular/common';

import { JsagaApiService } from '../core/jsaga-api.service';

@Component({
  selector: 'app-products-page',
  standalone: true,
  imports: [AsyncPipe, CurrencyPipe, NgClass],
  templateUrl: './products-page.component.html',
  styleUrl: './products-page.component.scss'
})
export class ProductsPageComponent {
  private readonly api = inject(JsagaApiService);
  protected readonly products$ = this.api.getProducts();
}
