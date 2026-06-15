import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Product {
  id: number;
  name: string;
  category: string;
  price: number;
  active: boolean;
}

export interface EventItem {
  id: number;
  name: string;
  eventDate: string;
  status: 'APERTO' | 'CHIUSO';
}

@Injectable({ providedIn: 'root' })
export class JsagaApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1';

  getProducts(active?: boolean): Observable<Product[]> {
    const query = active === undefined ? '' : `?active=${active}`;
    return this.http.get<Product[]>(`${this.baseUrl}/products${query}`);
  }

  getEvents(status?: 'APERTO' | 'CHIUSO'): Observable<EventItem[]> {
    const query = status ? `?status=${status}` : '';
    return this.http.get<EventItem[]>(`${this.baseUrl}/events${query}`);
  }
}
