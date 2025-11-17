# Frontend — React + TypeScript SPA

This is the frontend application for Global Airline League, built with React, TypeScript, and Vite.

## Technology Stack

- **Framework**: React 19 with TypeScript
- **Build Tool**: Vite 7
- **Mapping**: React Leaflet + OpenStreetMap tiles
- **Testing**: Vitest + React Testing Library
- **Linting**: ESLint with TypeScript support

## Getting Started

### Prerequisites

- Node.js 20+ and npm 10+
- Backend API server running on `http://localhost:8080` (or configure `VITE_API_BASE_URL`)

### Installation

```bash
cd frontend
npm install
```

### Development

Start the development server with hot module replacement:

```bash
npm run dev
```

The application will be available at `http://localhost:5173`. API requests to `/api/*` are automatically proxied to the backend at `http://localhost:8080`.

### Build

Create a production build:

```bash
npm run build
```

Build output will be in the `dist/` directory.

### Testing

Run tests:

```bash
npm test
```

### Type Checking

Run TypeScript type checking:

```bash
npm run typecheck
```

## Configuration

Create a `.env` file (copy from `.env.example`) if you need to customize the API base URL.

In development mode, Vite automatically proxies `/api/*` and `/ws/*` to the backend server.

## License

[To be determined]
