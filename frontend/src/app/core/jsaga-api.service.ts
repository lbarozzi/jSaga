import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Product {
  id: number;
  name: string;
  category: 'GASTRONOMIA' | 'BEVANDA' | 'DOLCE' | 'ALTRO';
  price: number;
  active: boolean;
}

export interface ProductRequest {
  name: string;
  category: 'GASTRONOMIA' | 'BEVANDA' | 'DOLCE' | 'ALTRO';
  price: number;
  active: boolean;
}

export interface EventItem {
  id: number;
  name: string;
  eventDate: string;
  status: 'APERTO' | 'CHIUSO';
}

export interface EventRequest {
  name: string;
  eventDate: string;
  status: 'APERTO' | 'CHIUSO';
}

@Injectable({ providedIn: 'root' })
export class JsagaApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/api/v1';

  // Products
  getProducts(active?: boolean): Observable<Product[]> {
    const query = active === undefined ? '' : `?active=${active}`;
    return this.http.get<Product[]>(`${this.baseUrl}/products${query}`);
  }

  getProduct(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/products/${id}`);
  }

  createProduct(req: ProductRequest): Observable<Product> {
    return this.http.post<Product>(`${this.baseUrl}/products`, req);
  }

  updateProduct(id: number, req: ProductRequest): Observable<Product> {
    return this.http.put<Product>(`${this.baseUrl}/products/${id}`, req);
  }

  setProductActive(id: number, value: boolean): Observable<Product> {
    return this.http.patch<Product>(`${this.baseUrl}/products/${id}/active?value=${value}`, null);
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/products/${id}`);
  }

  // Events
  getEvents(status?: 'APERTO' | 'CHIUSO'): Observable<EventItem[]> {
    const query = status ? `?status=${status}` : '';
    return this.http.get<EventItem[]>(`${this.baseUrl}/events${query}`);
  }

  createEvent(req: EventRequest): Observable<EventItem> {
    return this.http.post<EventItem>(`${this.baseUrl}/events`, req);
  }

  updateEvent(id: number, req: EventRequest): Observable<EventItem> {
    return this.http.put<EventItem>(`${this.baseUrl}/events/${id}`, req);
  }

  setEventStatus(id: number, value: 'APERTO' | 'CHIUSO'): Observable<EventItem> {
    return this.http.patch<EventItem>(`${this.baseUrl}/events/${id}/status?value=${value}`, null);
  }

  deleteEvent(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/events/${id}`);
  }
}
