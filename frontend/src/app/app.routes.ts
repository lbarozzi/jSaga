import { Routes } from '@angular/router';

import { CashierPageComponent } from './features/cashier-page.component';
import { DashboardPageComponent } from './features/dashboard-page.component';
import { EventsPageComponent } from './features/events-page.component';
import { ProductsPageComponent } from './features/products-page.component';
import { ReportsPageComponent } from './features/reports-page.component';

export const routes: Routes = [
	{ path: '', component: CashierPageComponent },
	{ path: 'dashboard', component: DashboardPageComponent },
	{ path: 'prodotti', component: ProductsPageComponent },
	{ path: 'eventi', component: EventsPageComponent },
	{ path: 'report', component: ReportsPageComponent },
	{ path: '**', redirectTo: '' }
];
