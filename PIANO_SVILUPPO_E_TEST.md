# Piano di sviluppo e test - Gestionale Festa Oratorio

Data: 2026-06-15
Progetto: jSaga (Spring Boot backend + Angular frontend)

## 1) Obiettivo del prodotto
Realizzare un sistema per la gestione vendite durante una festa di oratorio, con focus su:
- gestione prodotti gastronomici e bevande;
- emissione scontrino interno non fiscale;
- consultazione statistiche di venduto;
- utilizzo operativo su server locale con tablet/smartphone in LAN.

## 2) Requisiti raccolti (confermati)
- Eventi: supporto a piu eventi/serate.
- Pagamenti: solo contanti (MVP).
- Flusso cassa: selezione prodotti -> totale -> pagamento -> stampa scontrino.
- Magazzino: no scarico giacenze automatico nel MVP (solo report vendite).
- Ruoli: cassiere, admin.
- MVP prioritario:
  - acquisto e stampa scontrino;
  - statistiche venduto;
  - CRUD prodotti.
- Vincoli legali: ricevuta interna non fiscale.
- Test iniziali: essenziali (unit + smoke).
- Database: SQLite.
- Stampa: browser + supporto stampante termica ESC/POS nel MVP.
- Categorie: predefinite con filtri rapidi in cassa.
- Auth MVP: username/password semplice, senza recupero password.
- Annulli: consentito solo annullo prima della conferma vendita.
- Report: evento corrente + confronto base tra eventi.

## 3) Scope MVP (release 1)
### In scope
- Autenticazione base con ruoli `CASSIERE` e `ADMIN`.
- Anagrafica prodotti (CRUD): nome, categoria, prezzo, attivo/non attivo.
- Gestione evento: creazione evento e apertura/chiusura cassa evento.
- Vendita rapida:
  - aggiunta/rimozione righe carrello;
  - calcolo totale;
  - pagamento contanti;
  - generazione scontrino interno.
- Stampa scontrino:
  - formato HTML stampabile (browser print) + storico scontrini;
  - supporto stampante termica ESC/POS.
- Dashboard semplice vendite:
  - totale incassato;
  - numero scontrini;
  - top prodotti venduti;
  - confronto base tra eventi.

### Out of scope (post-MVP)
- POS / pagamenti elettronici.
- Gestione magazzino con scarico automatico giacenze.
- Offline-first completo con sincronizzazione conflitti.
- Integrazione fiscale.
- Storno post-conferma vendita.

## 4) Architettura proposta
## Backend (Spring Boot)
- Moduli logici:
  - Auth e utenti
  - Catalogo prodotti
  - Eventi
  - Vendite e scontrini
  - Reportistica
- API REST versionate (`/api/v1/...`).
- DTO separati da entity.
- Validazione input con Bean Validation.
- Error handling centralizzato (`@ControllerAdvice`).
- Persistenza: JPA/Hibernate con SQLite per MVP.

## Frontend (Angular)
- Aree applicative:
  - login;
  - cassa operativa (UI touch-friendly);
  - amministrazione prodotti/eventi;
  - dashboard statistiche.
- Struttura:
  - `core` (auth, interceptor, guard);
  - `shared` (componenti riusabili);
  - `features` (cashier, admin, reports).
- Stato applicativo iniziale: service-based + RxJS (senza over-engineering).

## 5) Modello dati iniziale (MVP)
Entita principali:
- User(id, username, passwordHash, role, enabled)
- Event(id, name, date, status)
- Product(id, name, category, price, active)
- Sale(id, eventId, operatorId, createdAt, totalAmount, paymentType=CASH)
- SaleItem(id, saleId, productId, productNameSnapshot, unitPrice, quantity, lineTotal)
- Receipt(id, saleId, progressiveNumber, printedAt)

Note:
- salvare snapshot nome/prezzo in `SaleItem` per preservare storico anche se il prodotto cambia;
- numerazione scontrino progressiva per evento.
- prevedere tabella categorie o enum estendibile per filtri rapidi UI.

## 6) Piano sviluppo per fasi
## Fase 0 - Setup e baseline (0.5-1 giorno)
- Definizione convenzioni (naming, branch strategy, PR checklist).
- Configurazione ambienti dev (backend + frontend).
- Setup CORS e profili Spring (`dev`, `test`, `prod`).
- Seed dati demo (prodotti base, utente admin/cassiere).

Deliverable:
- progetto avviabile in locale con login e health endpoint.

## Fase 1 - Dominio prodotti ed eventi (1-2 giorni)
- Backend:
  - CRUD prodotti con validazioni.
  - CRUD eventi con stato (APERTO/CHIUSO).
- Frontend:
  - schermata admin prodotti.
  - schermata admin eventi.

Deliverable:
- admin in grado di configurare catalogo e serata.

## Fase 2 - Flusso cassa e scontrino (2-3 giorni)
- Backend:
  - endpoint creazione vendita con righe.
  - calcolo totale server-side.
  - endpoint dettaglio/storico scontrini.
  - gestione annullo solo prima della conferma vendita.
- Frontend:
  - schermata cassa touch-friendly.
  - filtri rapidi per categoria prodotto.
  - riepilogo carrello e conferma pagamento contanti.
  - stampa scontrino via template HTML.
  - supporto stampa termica ESC/POS.

Deliverable:
- vendita completa end-to-end con scontrino stampabile.

## Fase 3 - Report base e hardening (1-2 giorni)
- Backend:
  - KPI vendite per evento.
  - KPI confronto base tra eventi.
  - endpoint top prodotti.
- Frontend:
  - dashboard sintetica.
- Hardening:
  - gestione errori e messaggi utente.
  - controllo autorizzazioni pagine/API.

Deliverable:
- MVP pronto per prova sul campo.

## 7) Piano test (Essenziale: Unit + Smoke)
## Obiettivo
Ridurre regressioni sulle funzionalita core (vendita e totale) mantenendo feedback rapido.

## Test backend (JUnit + Spring Test)
- Unit test servizi dominio:
  - calcolo totale vendita;
  - validazioni input;
  - numerazione progressiva scontrino.
- Repository test mirati (se logica query non banale).
- Smoke integration test API principali:
  - login;
  - CRUD prodotto base;
  - creazione vendita con 2-3 righe.

Target minimo iniziale:
- copertura logica business critica >= 70% sui service del dominio vendita.

## Test frontend (Angular)
- Unit test componenti/servizi critici:
  - carrello cassa;
  - calcolo subtotale/totale;
  - gestione stato prodotto attivo.
- Smoke test UI (manuale guidato o Cypress minimale):
  - login come cassiere;
  - creazione vendita;
  - stampa scontrino.

## Test di accettazione operativa (pre-evento)
Checklist rapida su rete locale reale:
- 2 dispositivi simultanei (tablet + smartphone);
- flusso vendita continuo per almeno 30 minuti;
- verifica stampa su stampante prevista;
- controllo report fine serata coerente con scontrini.

## 8) Definition of Done (DoD)
Una user story e completata quando:
- requisito funzionale implementato;
- test unitari pertinenti presenti e verdi;
- smoke test del flusso non regressivo;
- gestione errori utente minima presente;
- documentazione endpoint/UI aggiornata.

## 9) Backlog tecnico iniziale
- [ ] Definire schema DB iniziale e migrazioni (Flyway consigliato).
- [ ] Implementare security base (JWT o sessione server-side).
- [ ] Implementare CRUD prodotti backend + frontend.
- [ ] Implementare categorie prodotto con filtri rapidi in cassa.
- [ ] Implementare CRUD eventi backend + frontend.
- [ ] Implementare vendita e generazione scontrino.
- [ ] Implementare annullo solo pre-conferma.
- [ ] Implementare stampa scontrino HTML + ESC/POS.
- [ ] Implementare dashboard statistiche evento + confronto base eventi.
- [ ] Scrivere test unit + smoke MVP.
- [ ] Eseguire prova operativa su LAN.

## 10) Decisioni bloccate per MVP
1. Database: SQLite.
2. Stampa: browser + supporto ESC/POS subito.
3. Categorie predefinite: si, con filtri rapidi in cassa.
4. Autenticazione: semplice username/password.
5. Annulli: solo prima della conferma vendita.
6. Statistiche: evento corrente + confronto base tra eventi.

## 11) Prossimi passi operativi
- Avviare Fase 0 con setup ambienti e convenzioni.
- Creare backlog tecnico in issue/task separati per Fasi 1-3.
- Avviare implementazione Fase 1 (CRUD prodotti/eventi).
