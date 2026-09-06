// General rules
- Use Nx for monorepos (Angular + NestJS recommended).
- Use SCSS for styling.
- Follow a modular architecture (Feature Modules + Smart/Dumb Components).
- Use "apps" preset for Nx workspaces.
- Prefer manual implementation guidance over copy-pasting code for complex logic.
- Ensure strict typing (e.g., ReturnType<typeof setInterval>).
- Implement cleanup logic (OnModuleDestroy, unsubscribe from Observables).
- Validate HTTP endpoints and WebSocket Gateways.
- Use explicit folder structures (controllers/, services/, gateways/).

