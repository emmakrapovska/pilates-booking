# Pilates Studio – class booking

A small booking app for a pilates studio. The instructor publishes classes; clients pick one and book a spot
with their name and email. When a class runs out of spots it is shown as **Full** and no one else can book it,
even if several people try at the same moment.

**Stack:** Java 21 · Spring Boot 3.3 · PostgreSQL 16 · React 18 + TypeScript (Vite) · React Router 7

## Live demo

- **App (clients):** https://pilates-booking-psi.vercel.app
- **Instructor view:** https://pilates-booking-psi.vercel.app/instructor
- **API:** https://pilates-booking-api.onrender.com/api/classes

Hosted on Vercel (frontend), Render (backend) and Neon (PostgreSQL), all on free plans.

> The backend runs on Render's free plan and sleeps after ~15 minutes without traffic. The first page load after
> that can take up to a minute while it wakes up ("Loading classes…"); after that it is fast.

---

## Contents

- [Live demo](#live-demo)
- [Features](#features)
- [Project structure](#project-structure)
- [Running locally](#running-locally)
- [Configuration](#configuration)
- [API](#api)
- [How double booking is prevented](#how-double-booking-is-prevented)
- [AI confirmation message](#ai-confirmation-message)
- [Frontend overview](#frontend-overview)
- [Deploying](#deploying)
- [Design decisions](#design-decisions)
- [AI tools used](#ai-tools-used)

---

## Features

**Classes view** (`/`) – for clients
- Upcoming classes grouped by day, with start and end time, duration and spots left.
- **Book** opens a form (name + email); on success the client sees a short, friendly confirmation message
  written by an LLM (Google Gemini), see [AI confirmation message](#ai-confirmation-message).
- Full classes are crossed out and their button is disabled.

**Instructor view** (`/instructor`)
- Publish a class: name, date, start time, duration (30–90 min) and number of spots (1–30).
- See who booked each class (**Attendees**).
- Delete a class (a confirmation dialog warns how many bookings will be removed with it).

**Rules enforced by the backend**
- A class can't be created in the past, and can't be booked once it has started.
- No overbooking: once `booked == capacity`, further bookings get `409 Conflict`.
- The same email can't book the same class twice (case-insensitive).
- Data is stored in PostgreSQL, so it survives restarts.

---

## Project structure

```
pilates-booking/
├── docker-compose.yml          PostgreSQL for local development
├── backend/                    Spring Boot API
│   ├── Dockerfile
│   └── src/main/java/com/pilates/booking/
│       ├── PilatesBookingApplication.java
│       ├── config/             CorsConfig, TimeConfig (studio time zone Clock), GeminiConfig
│       ├── controller/         PilatesClassController, BookingController
│       │   └── handler/        ApiExceptionHandler → every error as {"message": "..."}
│       ├── domain/
│       │   ├── models/         JPA entities: PilatesClass, Booking
│       │   ├── dto/            request/response records
│       │   └── exceptions/     ApiException + one class per error case
│       ├── repository/         Spring Data JPA repositories
│       └── service/            BookingService, PilatesClassService, ConfirmationService (interfaces)
│           └── impl/           BookingServiceImpl, PilatesClassServiceImpl, ConfirmationServiceImpl
└── frontend/                   React SPA
    ├── vercel.json, public/_redirects   SPA fallback for hosting
    └── src/
        ├── main.tsx            entry point, wraps the app in <BrowserRouter>
        ├── App.tsx             header, tabs and routes
        ├── api.ts              fetch wrapper + API calls
        ├── types.ts            TypeScript types matching the backend DTOs
        ├── format.ts           date/time formatting, grouping by day
        ├── hooks/              useClasses (class list), useAsync (busy/error state)
        ├── pages/              ClassesPage
        └── components/
            ├── ClassList, BookingDialog, ConfirmDialog, HostPanel
            └── host/           CreateClassForm, HostClassList, AttendeesList
```

The backend follows a layered design: **controller → service → repository**, with entities, DTOs and exceptions
grouped under `domain/`. Controllers only translate HTTP to service calls; all rules live in the services.

---

## Running locally

**Requirements:** Java 21, Maven 3.9+, Node 20+, Docker (for PostgreSQL).

```bash
# 1. Database (PostgreSQL on localhost:5433)
docker compose up -d

# 2. Backend → http://localhost:8080
cd backend
mvn spring-boot:run

# 3. Frontend (in a new terminal)
cd frontend
cp .env.example .env
npm install
npm run dev
```

Open the URL Vite prints. The backend only accepts requests from the origin in `CORS_ORIGINS`
(default `http://localhost:5174`), so if Vite runs on a different port, start the backend with that origin, e.g.
`CORS_ORIGINS=http://localhost:5173 mvn spring-boot:run`.

The database schema is created automatically on first start.

To get AI-written confirmation messages, create a free API key in [Google AI Studio](https://aistudio.google.com/apikey)
and put it in `backend/.env` before starting the backend:

```bash
cd backend
cp .env.example .env    # then set GEMINI_API_KEY=<your key>
```

The backend reads `backend/.env` on startup (it is git-ignored); a real environment variable takes precedence.
Without a key the app works the same, but uses a fixed template message instead.

---

## Configuration

| Variable | Used by | Default | Description |
|---|---|---|---|
| `PORT` | backend | `8080` | HTTP port |
| `DB_URL` | backend | `jdbc:postgresql://localhost:5433/pilates` | JDBC connection string |
| `DB_USER` / `DB_PASSWORD` | backend | `pilates` / `pilates` | Database credentials |
| `APP_TIMEZONE` | backend | `Europe/Skopje` | Studio time zone; "has this class started?" is checked in this zone |
| `CORS_ORIGINS` | backend | `http://localhost:5174` | Comma-separated frontend origins allowed to call the API |
| `GEMINI_API_KEY` | backend | *(empty)* | Google AI Studio API key; empty = template confirmations, no LLM call |
| `GEMINI_MODEL` | backend | `gemini-3.5-flash-lite` | Gemini model used for confirmations (any model with a free tier works) |
| `VITE_API_URL` | frontend | `http://localhost:8080` | Backend base URL |
| `VITE_STUDIO_NAME` | frontend | `Pilates Studio` | Studio name shown in the header |

Backend variables can be set as environment variables or in `backend/.env` (see `backend/.env.example`).
Frontend variables are read at build time from `frontend/.env` (see `frontend/.env.example`). Never put the Gemini
key in the frontend: anything there is shipped to the browser.

---

## API

Base URL: `/api`. Request and response bodies are JSON. Times are local date-times in the studio's zone,
without an offset: `2026-11-02T18:30:00`.

| Method | Path | Description | Success |
|---|---|---|---|
| GET | `/classes` | Upcoming classes, ordered by start time | `200` `PilatesClassResponse[]` |
| POST | `/classes` | Create a class | `201` `PilatesClassResponse` |
| DELETE | `/classes/{id}` | Delete a class and its bookings | `204` |
| POST | `/classes/{id}/bookings` | Book a spot | `201` `BookingResponse` |
| GET | `/classes/{id}/bookings` | Attendee list, oldest first | `200` `BookingResponse[]` |
| GET | `/classes/{id}/bookings/{bookingId}/confirmation` | Friendly confirmation message (LLM) | `200` `ConfirmationResponse` |

### Request bodies

```jsonc
// POST /api/classes
{ "title": "Mat Pilates", "startTime": "2026-11-02T18:30:00", "durationMinutes": 55, "capacity": 8 }
//  title: 1–80 chars · durationMinutes: 15–240 · capacity: 1–30 · startTime: must be in the future

// POST /api/classes/{id}/bookings
{ "name": "Ana Petrovska", "email": "ana@example.com" }
//  name: 1–100 chars · email: valid, up to 160 chars (stored trimmed and lower-case)
```

### Responses

```jsonc
// PilatesClassResponse
{ "id": 1, "title": "Mat Pilates", "startTime": "2026-11-02T18:30:00", "durationMinutes": 55,
  "capacity": 8, "booked": 3, "spotsLeft": 5, "available": true }

// BookingResponse
{ "id": 7, "classId": 1, "name": "Ana Petrovska", "email": "ana@example.com",
  "createdAt": "2026-10-26T15:04:11.123Z" }

// ConfirmationResponse (aiGenerated = false when the template fallback was used)
{ "message": "Hi Ana, you're all set for Mat Pilates on Monday, 2 November at 18:30. We can't wait to see you!",
  "aiGenerated": true }
```

### Errors

Every error has the same shape, so the frontend can show `message` directly:

```json
{ "message": "Sorry, this class just got fully booked." }
```

| Status | When | Exception |
|---|---|---|
| `400` | Invalid request body (message lists the fields, e.g. `email: must be a well-formed email address`) | validation |
| `400` | Creating a class in the past | `PilatesClassStartTimeInPastException` |
| `404` | Class doesn't exist | `PilatesClassNotFoundException` |
| `404` | Booking doesn't exist (or belongs to another class) | `BookingNotFoundException` |
| `409` | Class has already started | `PilatesClassAlreadyStartedException` |
| `409` | Same email already booked this class | `DuplicateBookingException` |
| `409` | No spots left | `PilatesClassFullyBookedException` |

All of these extend `ApiException`, which carries the HTTP status; `ApiExceptionHandler` turns them into the JSON above.

---

## How double booking is prevented

The risky case is two people booking the last spot at the same moment. Without care, both requests could count
"7 of 8 booked", both pass the check, and the class ends up with 9 people.

`BookingServiceImpl.book` runs in one transaction and starts by loading the class with a **pessimistic write lock**
(`PilatesClassRepository.findByIdForUpdate`, i.e. `SELECT … FOR UPDATE`). The second request waits on that row until the
first one commits, then counts again, sees the class is full and gets `409`. The checks in order:

1. Class exists → otherwise `404`.
2. Class hasn't started → otherwise `409`.
3. Email hasn't booked this class → otherwise `409`.
4. `booked < capacity` → otherwise `409`.
5. Save the booking.

A unique constraint on `(pilates_class_id, email)` in the `bookings` table is a second safety net for duplicates.

---

## AI confirmation message

After a successful booking the frontend calls
`GET /api/classes/{classId}/bookings/{bookingId}/confirmation` and shows the result in the booking dialog
("Writing your confirmation…" while it loads).

`ConfirmationServiceImpl`:

1. Loads the booking together with its class (one query, `join fetch`).
2. Sends the client's first name, class name, day, start time and duration to the Gemini API
   (`POST https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent`, key in the
   `x-goog-api-key` header). A system instruction asks for 2 warm sentences, plain text, using the details exactly
   as given and inventing nothing (no address, price, etc.). The email is not sent to the LLM.
3. Returns the generated text with `aiGenerated: true`.

**Fallback:** if `GEMINI_API_KEY` is not set, Gemini returns an error or empty text, or doesn't answer within
15 seconds, the endpoint returns a template message (`aiGenerated: false`) and logs a warning. The LLM is never on the
critical path: the booking is already saved before the message is requested, so a slow or failing LLM can't break or
delay booking.

**Model and cost:** the default `gemini-3.5-flash-lite` has a free tier in Google AI Studio, so no credits are needed.
Free-tier limits are per project and change over time; check them in AI Studio. The model can be changed with
`GEMINI_MODEL`.

---

## Frontend overview

- **Routing** (`App.tsx`): `/` → `ClassesPage`, `/instructor` → `HostPanel`, anything else redirects to `/`.
  The tabs are `NavLink`s, so the active tab follows the URL and survives a refresh.
- **Data** (`hooks/useClasses.ts`): loads the class list once and exposes `refresh()`. After any change (booking,
  creating or deleting a class) the list is reloaded, so spot counts are always the server's numbers.
- **Async actions** (`hooks/useAsync.ts`): one hook for the "busy → call API → show error" pattern used by every form
  and button. `run(action)` resolves to `true` on success.
- **Dialogs** use the native `<dialog>` element (`BookingDialog`, `ConfirmDialog`), so focus trapping, Escape and the
  backdrop come from the browser.
- **Errors** from the API are shown where the action happened: in the form, in the dialog, or as a banner if the
  class list can't be loaded.

There is no global state library or Context: the component tree is shallow and props are passed at most two levels.

---

## Deploying

- **Database:** a managed PostgreSQL (e.g. Neon or Supabase). Put its JDBC URL and credentials in `DB_URL`,
  `DB_USER`, `DB_PASSWORD`.
- **Backend:** any Docker host (e.g. Render) using `backend/Dockerfile`. Set `DB_*`, `CORS_ORIGINS=<frontend URL>`,
  `GEMINI_API_KEY` and, if needed, `APP_TIMEZONE`. The port is taken from `PORT`.
- **Frontend:** a static host (e.g. Vercel or Netlify) with root `frontend`, build command `npm run build`, output
  `dist`, and `VITE_API_URL=<backend URL>`. `vercel.json` and `public/_redirects` send every path to `index.html`,
  so opening `/instructor` directly works.

---

## Design decisions

- **Capacity instead of one-person slots.** Pilates classes are usually group sessions, so a class has a number of
  spots (1 = private session). "Available" means at least one spot is left.
- **Overbooking is prevented in the database layer**, with a row lock, not in the frontend: the UI can always be
  out of date, the database can't.
- **One time zone.** Times are stored as local date-times, as the brief allows. "Is it in the future?" is checked
  with an injected `Clock` set to `APP_TIMEZONE`, so it is right on cloud servers running in UTC.
- **One exception class per error case**, all extending `ApiException`, so services read as business rules
  (`throw new PilatesClassFullyBookedException()`) and one handler maps them to HTTP.
- **Service interfaces + implementations** keep controllers dependent on a contract, not on the implementation.
- **No login.** The brief doesn't require authentication, so the Instructor view is open to anyone with the link.
- **Schema via Hibernate `ddl-auto=update`**: simple for a project this size; Flyway would be the next step.

---

## AI tools used

Built with Claude (Claude Code) for scaffolding the Spring Boot and React code, which I then reviewed and adapted.
<!-- Add here what you changed/decided yourself. -->