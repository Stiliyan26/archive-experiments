# Successful Project Setup Commands

These are the commands that were successfully used to set up the live-stock-prices repository.

### 1. Create Workspace
```powershell
# Create an empty Nx workspace
npx create-nx-workspace@latest live-stock-prices --preset=apps --packageManager=npm --nxCloud=skip --skipGit

# Enter the directory
cd live-stock-prices

# Install required plugins
npm install -D @nx/nest @nx/angular @nx/js
```

### 2. Generate Applications
```powershell
# Generate NestJS API (without linking frontend yet)
npx nx g @nx/nest:app --name=api --directory=apps/api --linter=eslint --unitTestRunner=jest

# Generate Angular Client (manually linking dependencies if needed)
# Note: If you encounter peer dependency issues, run: npm install --legacy-peer-deps
npx nx g @nx/angular:app --name=client --directory=apps/client --style=scss --routing --ssr=false --linter=eslint --unitTestRunner=jest
```

### 3. Generate Shared Library
```powershell
# Generate the TypeScript interface library
npx nx g @nx/js:lib api-interfaces --directory=libs/api-interfaces --importPath=@live-stock-prices/api-interfaces
```

### 4. Generate Backend Structure (In Progress)
```powershell
# Create the Stocks module components
npx nx g @nx/nest:module stocks --directory=apps/api/src/app/stocks
npx nx g @nx/nest:controller stocks --directory=apps/api/src/app/stocks
npx nx g @nx/nest:service stocks --directory=apps/api/src/app/stocks
npx nx g @nx/nest:gateway stocks/stocks --directory=apps/api/src/app/stocks
```

