# Game of Life – Full Stack Setup

This repository contains:

- `game-of-life/`: Angular standalone application that renders Conway’s Game of Life and now supports saving/loading named patterns.
- `life-patterns-service/`: NestJS API backed by MongoDB for persisting pattern data.

You can run the stack locally (Angular + Nest + MongoDB) or via Docker Compose. The instructions below cover both approaches and detail the required environment variables.

---

## 1. Prerequisites

- **Local run:** Node.js ≥ 20, npm ≥ 10, and a MongoDB instance (local or remote).
- **Docker run:** Docker Desktop (or Docker Engine) with Docker Compose v2.

---

## 2. Environment Configuration

### Backend (`life-patterns-service`)

Create a `.env` file inside `life-patterns-service/` (copy from the example below):

```
# NestJS API
API_PORT=3001
API_CORS_ORIGINS=http://localhost:4200

# MongoDB Connection
MONGODB_URI=mongodb://localhost:27017/life-patterns
```

- `API_PORT` – port the Nest app listens on.
- `API_CORS_ORIGINS` – comma‑separated origins allowed to call the API. Include the Angular dev server (`http://localhost:4200`) and any additional hosts as needed.
- `MONGODB_URI` – connection string to MongoDB. Change only if you run MongoDB on another host or port.

> The backend reads these values through `ConfigModule` and validates them via `Joi`, so missing/invalid entries will be reported clearly at startup.

### Frontend (`game-of-life`)

Angular uses the files in `src/environments/`:

- `environment.development.ts` – used by `ng serve`, currently points to `http://localhost:3001`.
- `environment.ts` – production build (also points to the same API). Adjust if you deploy the API elsewhere.

If you need different URLs per environment, update these files or introduce additional configurations.

---

## 3. Running Locally (without Docker)

### 3.1 Start MongoDB

Ensure a MongoDB instance is available on `mongodb://localhost:27017`. You can:

- Install MongoDB locally, **or**
- Run a temporary container:

  ```bash
  docker run --rm -p 27017:27017 --name gol-mongo mongo:7
  ```

### 3.2 Start the backend

```bash
cd life-patterns-service
npm install
npm run start:dev
```

The API starts on `http://localhost:3001`. Logs confirm the Mongo connection and CORS origins.

### 3.3 Start the frontend

Open a new terminal:

```bash
cd game-of-life
npm install
npm start
```

Navigate to http://localhost:4200. The app auto-connects to the API URL defined in `environment.development.ts`.

---

## 4. Running with Docker Compose

From the repository root:

```bash
docker compose up --build
```

This spins up:

- `game-of-life-frontend` → Angular build served by Nginx at http://localhost:4200
- `game-of-life-backend` → Nest API at http://localhost:3001
- `game-of-life-mongo` → MongoDB listening on host port 27018 (mapped from container 27017). Connect via `mongodb://localhost:27018/life-patterns`.

Named resources:

- Volume: `game-of-life-mongo-data` (persists Mongo collections between restarts)
- Network: `game-of-life-network`

Stop everything with:

```bash
docker compose down
```

Add `--volumes` if you want to wipe the Mongo data.

---

## 5. Testing

### Frontend

Inside `game-of-life/`:

```bash
npm run lint
npm test
```

The new specs cover:

- `PatternService` – ensures each REST call uses the correct URL/method/body.
- `GameOfLifeStore` – validates pattern load/save/apply flows, success/error handling, and signal state transitions.

### Backend

Inside `life-patterns-service/`:

```bash
npm run lint
npm test
npm run test:e2e
```

E2E tests use `mongodb-memory-server`, so no external Mongo instance is required for them.

---

## 6. Deployment Notes

- **Frontend build:** `npm run build` inside `game-of-life` produces a static bundle in `dist/game-of-life/browser/`. In Docker we serve it through Nginx, but you can deploy to any static hosting provider.
- **Backend deployment:** Use `npm run build` in `life-patterns-service`, then `npm run start:prod`. Ensure environment variables match your managed MongoDB instance and allowed CORS origins.
- **Environment updates:** If the API URL changes, update:
  - Docker `API_CORS_ORIGINS` value,
  - Angular environment files (or add a runtime config mechanism),
  - Any external clients (e.g., MongoDB Compass).

---

## 7. Troubleshooting

- **Port conflicts:** If you already run services on ports 3001 or 27017, adjust `.env`/compose mappings (e.g., expose Mongo on `27018` as done in `docker-compose.yml`).
- **CORS errors:** Confirm `API_CORS_ORIGINS` includes the origin shown in the browser console.
- **Mongo connectivity:** The API logs the target URI at startup. If it cannot connect, double-check the host, port, and credentials.

Feel free to extend this document as new deployment targets or configuration options are added.

