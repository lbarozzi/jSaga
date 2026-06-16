# Piano di sviluppo e test - Gestionale Festa Oratorio

Data aggiornamento: 2026-06-16
Progetto: jSaga (Spring Boot 4.1 backend + Angular 20 frontend)

---

## 1) Obiettivo del prodotto
Sistema per la gestione vendite durante una festa di oratorio:
- gestione prodotti gastronomici e bevande;
- emissione scontrino interno non fiscale;
- consultazione statistiche di venduto;
- utilizzo operativo su server locale con tablet/smartphone in LAN.

---

## 2) Requisiti raccolti (confermati)
- Eventi: supporto a più eventi/serate.
- Pagamenti: solo contanti (MVP).
- Flusso cassa: selezione prodotti → totale → pagamento → stampa scontrino.
- Magazzino: no scarico giacenze automatico nel MVP (solo report vendite).
- Ruoli: cassiere, admin.
- MVP prioritario: acquisto e stampa scontrino; statistiche venduto; CRUD prodotti.
- Vincoli legali: ricevuta interna non fiscale.
- Test iniziali: essenziali (unit + smoke).
- Database: SQLite.
- Stampa: browser + supporto stampante termica ESC/POS nel MVP.
- Categorie: predefinite con filtri rapidi in cassa.
- Auth MVP: username/password semplice, senza recupero password.
- Annulli: consentito solo annullo prima della conferma vendita.
- Report: evento corrente + confronto base tra eventi.

---

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

---

## 4) Architettura

### Backend (Spring Boot 4.1, Java 21)
- Moduli logici: Auth/Utenti · Catalogo prodotti · Eventi · Vendite/Scontrini · Reportistica
- API REST versionate (`/api/v1/...`).
- DTO separati da entity. Bean Validation. `@ControllerAdvice` globale.
- Persistenza: JPA/Hibernate + SQLite (`./data/jsaga.db`).
- Stampa ESC/POS: `EscPosSerialPrintService` (abilitabile via `application.properties`).

### Frontend (Angular 20)
- Standalone components + Signals (no NgRx).
- Struttura: `core/` (service API) · `features/` (pagine).
- Navigazione: Cassa come home (`/`), Admin dropdown (Dashboard/Prodotti/Eventi/Report).
- Stato applicativo: service-based + RxJS.

### Build & Deploy
- **Dev**: `./mvnw spring-boot:run` + `cd frontend && npm start` (proxy `/api` → `:8080`).
- **Produzione (JAR unico)**: `./mvnw package -DskipTests` → `java -jar target/jSaga-*.jar`.
- `frontend-maven-plugin` 1.15.1: `node install` → `npm ci` → `ng build` → `src/main/resources/static/`.
- `SpaRoutingFilter`: forward GET senza estensione non-API → `/index.html`.
- `DataInitializer`: seed 14 prodotti + 1 evento al primo avvio (DB vuoto).

---

## 5) Modello dati (MVP)
| Entità | Campi principali |
|--------|-----------------|
| User | id, username, passwordHash, role (CASSIERE/ADMIN), enabled |
| Event | id, name, eventDate, status (APERTO/CHIUSO) |
| Product | id, name, category (GASTRONOMIA/BEVANDA/DOLCE/ALTRO), price, active |
| Sale | id, eventId, operatorId, createdAt, totalAmount, paymentType=CASH |
| SaleItem | id, saleId, productId, productNameSnapshot, unitPrice, quantity, lineTotal |
| Receipt | id, saleId, progressiveNumber, printedAt |

Note:
- snapshot nome/prezzo in `SaleItem` per preservare storico storico.
- numerazione scontrino progressiva per evento.

---

## 6) Stato avanzamento per fasi

### Fase 0 - Setup e baseline ✅ COMPLETATA
- [x] Configurazione ambienti dev (backend + frontend).
- [x] Setup CORS e proxy Angular dev server.
- [x] Seed dati demo (`DataInitializer`).
- [x] Build automatizzato (`frontend-maven-plugin`).
- [x] SPA routing in produzione (`SpaRoutingFilter`).

### Fase 1 - Dominio prodotti ed eventi ✅ COMPLETATA
- [x] CRUD prodotti backend (GET/POST/PUT/PATCH/DELETE).
- [x] CRUD eventi backend (GET/POST/PUT/PATCH/DELETE).
- [x] Frontend: schermata prodotti con tabella, modal form, toggle attivo, elimina con conferma.
- [x] Frontend: schermata eventi con tabella, modal form, toggle stato, elimina con conferma.

### Fase 2 - Flusso cassa e scontrino 🔶 IN CORSO
- [x] Frontend cassa: griglia prodotti touch-friendly con filtri categoria.
- [x] Frontend cassa: carrello sticky con add/remove/qty, totale live.
- [x] Frontend cassa: modal scontrino HTML stampabile (browser print) — *mock locale, no backend*.
- [ ] **Backend**: endpoint `POST /api/v1/sales` (creazione vendita con righe).
- [ ] **Backend**: calcolo totale server-side.
- [ ] **Backend**: `GET /api/v1/sales/{id}` (dettaglio scontrino).
- [ ] **Backend**: `GET /api/v1/sales?eventId=X` (storico vendite per evento).
- [ ] **Backend**: numerazione scontrino progressiva per evento.
- [ ] **Frontend**: collegare il "Conferma pagamento" a `POST /api/v1/sales` (oggi è mock).
- [ ] **Frontend**: storico scontrini per evento.
- [ ] **Stampa**: supporto stampante termica ESC/POS (backend già predisposto).

### Fase 3 - Report base e hardening ⬜ DA INIZIARE
- [ ] Backend: KPI vendite per evento (totale incassato, nr scontrini, top prodotti).
- [ ] Backend: confronto base tra eventi.
- [ ] Frontend: dashboard statistiche collegata a API vendite reali.
- [ ] Frontend: pagina Report completata.
- [ ] Gestione errori utente (toast/banner su errori API).
- [ ] Controllo autorizzazioni pagine/API (auth e guard route).

### Auth ⬜ DA INIZIARE
- [ ] Backend: entità User + Spring Security (username/password, ruoli CASSIERE/ADMIN).
- [ ] Backend: login endpoint + sessione/JWT.
- [ ] Frontend: pagina login.
- [ ] Frontend: guard route (admin protetto).
- [ ] Frontend: header/interceptor con token.

---

## 7) Piano test

### Backend (JUnit + Spring Test)
- Unit test servizi dominio: calcolo totale vendita, validazioni input, numerazione progressiva.
- Repository test (se logica query non banale).
- Smoke integration test: login, CRUD prodotto base, creazione vendita 2-3 righe.
- Target: copertura logica business critica ≥ 70% sui service del dominio vendita.

### Frontend (Angular)
- Unit test: carrello cassa, calcolo subtotale/totale, stato prodotto attivo.
- Smoke test UI (manuale o Cypress minimale): login, creazione vendita, stampa scontrino.

### Test di accettazione operativa (pre-evento)
- 2 dispositivi simultanei (tablet + smartphone).
- Flusso vendita continuo per almeno 30 minuti.
- Verifica stampa su stampante prevista.
- Controllo report fine serata coerente con scontrini.

---

## 8) Definition of Done (DoD)
Una user story è completata quando:
- requisito funzionale implementato;
- test unitari pertinenti presenti e verdi;
- smoke test del flusso non regressivo;
- gestione errori utente minima presente;
- documentazione endpoint/UI aggiornata.

---

## 9) Backlog tecnico aggiornato

### Priorità ALTA (blocca MVP)
- [ ] Backend modulo Vendite: `Sale`, `SaleItem`, `Receipt` + endpoint REST.
- [ ] Backend: numerazione progressiva scontrino per evento.
- [ ] Frontend: collegare cassa a `POST /api/v1/sales`.
- [ ] Auth: Spring Security + login frontend.

### Priorità MEDIA
- [ ] Dashboard KPI vendite reali (dopo modulo Vendite).
- [ ] Pagina Report completata.
- [ ] Storico scontrini per evento.
- [ ] Stampa ESC/POS dal frontend.
- [ ] Gestione errori API (toast/banner globale via interceptor Angular).

### Priorità BASSA (post-MVP)
- [ ] Skip npm ci con property Maven (`-Dfrontend.skip.install=true`) per avvii veloci.
- [ ] Profilo Spring `prod` con logging ridotto.
- [ ] Test unit frontend (carrello, calcolo totale).
- [ ] Smoke Cypress minimale.

---

## 10) Comandi di riferimento

```bash
# Dev (due terminali)
./mvnw spring-boot:run              # backend :8080 (+ Angular build automatico)
cd frontend && npm start            # dev server :4200 con proxy + hot reload

# Produzione
./mvnw package -DskipTests         # build tutto → JAR
java -jar target/jSaga-*.jar       # serve su :8080

# Solo Angular
cd frontend && npm run build        # output → src/main/resources/static/
```

---

## 11) Decisioni architetturali prese
1. Database: SQLite (`./data/jsaga.db`, gitignored).
2. Stampa: browser print (implementato) + ESC/POS (predisposto, da collegare).
3. Categorie prodotto: enum fisso GASTRONOMIA/BEVANDA/DOLCE/ALTRO con filtri rapidi in cassa.
4. Autenticazione: username/password semplice — da implementare nella prossima fase.
5. Annulli: solo prima della conferma vendita.
6. Statistiche: evento corrente + confronto base tra eventi.
7. Build: `frontend-maven-plugin` integrato in Maven, `ng build` → `static/`.
8. SPA routing: `SpaRoutingFilter` (forward → `index.html` per route non-API senza estensione).
9. Stato Angular: Signals + service-based, nessun NgRx.
10. `spring-boot:run` include automaticamente il build Angular (lifecycle fork a `test-compile`).