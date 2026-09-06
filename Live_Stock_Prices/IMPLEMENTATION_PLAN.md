# Live Stock Prices Dashboard - Implementation Plan

## Phase 1: Project Setup
- [x] **Step 1:** Create Nx Workspace
  ```bash
  # 1. Clean up failed attempt (if folder exists)
  Remove-Item -Recurse -Force live-stock-prices
  
  # 2. Create the workspace using 'apps' preset (empty monorepo)
  npx create-nx-workspace@latest live-stock-prices --preset=apps --packageManager=npm --nxCloud=skip --skipGit
  
  # 3. Go into the new directory
  cd live-stock-prices
  
  # 4. Install plugins
  npm install -D @nx/nest @nx/angular @nx/js
  ```
- [x] **Step 2:** Generate Applications
  ```bash
  # Generate NestJS API
  # NOTE: We remove --frontendProject=client for now because 'client' doesn't exist yet.
  # We can link them later in nx.json or angular.json if needed.
  npx nx g @nx/nest:app --name=api --directory=apps/api --linter=eslint --unitTestRunner=jest
  
  # Generate Angular Client
  npx nx g @nx/angular:app --name=client --directory=apps/client --style=scss --routing --ssr=false --linter=eslint --unitTestRunner=jest
  ```
- [x] **Step 3:** Generate Shared Library
  ```bash
  # Generate a TypeScript library (nx/js handles both TS/JS libraries)
  npx nx g @nx/js:lib api-interfaces --directory=libs/api-interfaces --importPath=@live-stock-prices/api-interfaces
  ```

## Phase 2: Architecture & Structure
### Backend Structure (`apps/api/src/app`)
We will organize by **domain module** rather than a flat structure.
```text
apps/api/src/app/
├── stocks/
│   ├── dto/                # Data Transfer Objects
│   ├── entities/           # Database/Memory Entities
│   ├── stocks.controller.ts # REST Endpoints (Polling, Long Polling, SSE)
│   ├── stocks.gateway.ts    # WebSocket Gateway
│   ├── stocks.service.ts    # Business Logic & Data Store
│   └── stocks.module.ts     # Module definition
└── app.module.ts           # Root module imports StocksModule
```

### Frontend Structure (`apps/client/src/app`)
We will use a **feature-based** architecture with "Smart" (Container) and "Dumb" (Presentational) components.
```text
apps/client/src/app/
├── core/                   # Singleton services, interceptors, guards
│   └── services/
│       └── stock-data.service.ts # Handles all 4 data fetching strategies
├── features/
│   └── dashboard/
│       ├── dashboard.component.ts    # Smart Component (subscribes to data)
│       ├── dashboard.component.html
│       └── components/
│           ├── stock-list/           # Dumb Component (displays table)
│           └── strategy-selector/    # Dumb Component (buttons)
├── shared/                 # Reusable UI components (optional)
└── app.component.ts        # Root
```

## Phase 3: Shared Logic
- [x] **Step 4:** Define `Stock` Interface
  - File: `libs/api-interfaces/src/lib/api-interfaces.ts`

## Phase 4: Backend Implementation
- [ ] **Step 5:** Create `StocksModule` structure
  ```bash
  # Nx generators infer the project from the directory path, no need for --project usually if directory is precise
  # But if needed, --project=api should work.
  # If it fails, try cd apps/api first or rely on directory.
  
  cd live-stock-prices
  npx nx g @nx/nest:module stocks --directory=apps/api/src/app/stocks
  npx nx g @nx/nest:controller stocks --directory=apps/api/src/app/stocks
  npx nx g @nx/nest:service stocks --directory=apps/api/src/app/stocks
  npx nx g @nx/nest:gateway stocks/stocks --directory=apps/api/src/app/stocks
  ```
- [ ] **Step 6:** Implement `StockService` (Mock Data & Logic)
- [ ] **Step 7:** Implement Polling Endpoint
- [ ] **Step 8:** Implement Long Polling Endpoint
- [ ] **Step 9:** Implement SSE Endpoint
- [ ] **Step 10:** Implement WebSocket Gateway

## Phase 5: Frontend Implementation
- [ ] **Step 11:** Create Feature Structure
  ```bash
  npx nx g @nx/angular:component features/dashboard --project=client
  npx nx g @nx/angular:service core/services/stock-data --project=client
  ```
- [ ] **Step 12:** Implement `StockDataService` (Client Methods)
- [ ] **Step 13:** Implement Dashboard Smart Component
- [ ] **Step 14:** Implement Child Components
