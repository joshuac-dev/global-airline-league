# Frontend Overview

## Introduction

The Global Airline League frontend is a single-page application (SPA) built with React, TypeScript, and Vite. This document provides an overview of the frontend architecture, component responsibilities, and extension points for future development.

## Technology Stack

### Core Framework
- **React 18**: Stable React with hooks and concurrent features
- **TypeScript**: Strict mode enabled for type safety
- **Vite 7**: Fast build tool with HMR

### Mapping & Visualization
- **React Leaflet 4.2**: React bindings for Leaflet.js
- **Leaflet 1.9**: Open-source JavaScript library for interactive maps
- **OpenStreetMap Tiles**: Free tile service (https://tile.openstreetmap.org)

### Testing
- **Vitest 4**: Fast unit test framework compatible with Vite
- **React Testing Library 16**: Component testing with user-centric queries
- **jsdom**: Browser environment simulation for tests

### Code Quality
- **ESLint 9**: Linting with TypeScript and React plugins
- **TypeScript Strict Mode**: Enforces type safety (no implicit any, strict null checks)
- **.editorconfig**: Consistent editor settings (2-space indentation, UTF-8, LF line endings)

## Project Structure

```
frontend/
├── src/
│   ├── api/               # API client functions
│   ├── components/        # React components
│   ├── hooks/             # Custom React hooks
│   ├── types/             # TypeScript type definitions
│   ├── test/              # Test setup and utilities
│   ├── App.tsx            # Root application component
│   ├── index.css          # Global styles and CSS reset
│   └── main.tsx           # Application entry point
├── public/                # Static assets
├── .env.example           # Environment variable template
├── package.json           # Dependencies and scripts
├── vite.config.ts         # Vite configuration
└── tsconfig.json          # TypeScript configuration
```

## Component Responsibilities

### MapView (`src/components/MapView.tsx`)

**Purpose**: Renders the interactive OpenStreetMap with airport markers.

**Props**:
- `airports: Airport[]` - Array of airports to display as markers
- `selectedAirportId: number | null` - Currently selected airport (highlighted marker)
- `onMarkerClick?: (airport: Airport) => void` - Callback when marker is clicked

**Features**:
- OSM raster tiles with proper attribution
- Marker rendering with custom icons (default vs. selected/highlighted)
- Popup displaying airport details (name, IATA/ICAO, city, country)
- Fly-to animation when selected airport changes
- Responsive container (full viewport height minus header)

**Future Extensions**:
- Marker clustering (use `react-leaflet-markercluster`)
- Viewport-based lazy loading (load airports only in visible bounds)
- Custom marker sprites for different airport sizes
- Route polylines overlay
- Demand heatmap layer

**Technical Notes**:
- Uses Leaflet's default marker icons with fixed asset paths for bundled environment
- Highlighted marker uses a blue icon from leaflet-color-markers
- `FlyToAirport` sub-component handles map.flyTo() when selection changes

### SearchBox (`src/components/SearchBox.tsx`)

**Purpose**: Provides a debounced search interface for finding airports.

**Props**:
- `onSelectAirport: (airport: Airport) => void` - Callback when user selects an airport from results

**Features**:
- 300ms debounce on search input to reduce API calls
- Abort controller pattern to cancel stale requests
- Dropdown results list with keyboard navigation support
- "No matches" state for empty results
- Loading indicator during search
- Accessible ARIA labels and roles

**State Management**:
- `query`: Current search input value
- `results`: Array of matching airports
- `isLoading`: Search in-progress indicator
- `isOpen`: Controls dropdown visibility

**Future Extensions**:
- Recent searches history (localStorage)
- Keyboard shortcuts (Cmd+K to focus)
- Advanced filters (country, airport size, region)
- Client-side result caching

### App (`src/App.tsx`)

**Purpose**: Root application component orchestrating state and child components.

**State**:
- `airports: Airport[]` - Accumulated list of loaded airports
- `selectedAirportId: number | null` - Currently selected airport
- `isLoading: boolean` - Data loading state
- `error: string | null` - Error message if fetch fails
- `hasMore: boolean` - Whether more airports can be loaded
- `offset: number` - Current pagination offset

**Responsibilities**:
- Initial airport data fetch on mount
- Pagination control ("Load More" button)
- Error handling and retry logic
- Coordinate between SearchBox and MapView
- Header/layout structure

**Future Extensions**:
- User authentication UI
- Airline management dashboard
- Navigation menu
- Theme switcher
- Real-time notifications

## API Client (`src/api/airports.ts`)

**Functions**:
- `fetchAirports(params, signal?)` - Paginated airport list
- `searchAirports(params, signal?)` - Airport search by query

**Configuration**:
- Base URL from `VITE_API_BASE_URL` env var (defaults to empty for proxy)
- Accepts `AbortSignal` for request cancellation

**Error Handling**:
- Throws descriptive errors on non-ok responses
- Properly handles AbortError for cancelled requests

## Type Definitions (`src/types/airport.ts`)

**Airport Entity**:
```typescript
interface Airport {
  id: number;
  name: string;
  iata: string | null;
  icao: string | null;
  city: string;
  countryCode: string;
  latitude: number;
  longitude: number;
}
```

**Request Parameters**:
- `AirportListParams` - offset, limit, country filter
- `AirportSearchParams` - query string, limit

## State Management

### Current Approach
- React built-in hooks (`useState`, `useEffect`)
- Local component state
- Props drilling for shared state

**Rationale**: Simple and sufficient for initial feature set (airport map + search).

### Future Migration Path

When state complexity grows (user sessions, multiple views, real-time updates), consider:

**Option 1: React Query / TanStack Query**
- Server state caching and synchronization
- Automatic refetching and background updates
- Optimistic updates
- **Best for**: API-driven state with WebSocket integration

**Option 2: Zustand**
- Lightweight global state (< 1KB)
- Simple API without boilerplate
- Good TypeScript support
- **Best for**: Client-side UI state management

**Option 3: Redux Toolkit**
- Standardized patterns
- DevTools integration
- Middleware ecosystem
- **Best for**: Large-scale applications with complex state interactions

## Routing

**Current**: Single-page view (map + search).

**Future**: Add React Router when multiple views are needed:
- `/` - Dashboard / Map view
- `/airline` - Airline management
- `/routes` - Route planning
- `/fleet` - Aircraft fleet
- `/finance` - Financial reports
- `/rankings` - Leaderboards

## Styling Approach

### Current Implementation
- Global CSS reset (`index.css`)
- CSS variables for theming (`:root`)
- Component-scoped CSS files (`Component.css`)
- Minimal utility classes

**Rationale**: Simple, fast, and no build-time overhead.

### Future Options

When design system needs grow, consider:
- **CSS Modules**: Scoped styles (already supported by Vite)
- **Tailwind CSS**: Utility-first framework for rapid UI development
- **Styled Components**: CSS-in-JS for dynamic styling
- **shadcn/ui**: Pre-built accessible components (Radix UI + Tailwind)

## WebSocket Integration (Placeholder)

### Current State
- `useWorldSocket` hook stub in `src/hooks/useWorldSocket.ts`
- Returns `{ connected: false, error: null }`
- Comments reference future WebSocket endpoints

### Future Implementation

**Backend Endpoints** (see `repo-catalogue/airline-web__app__websocket.md`):
- `/ws/world` - Global game state updates (tick notifications, oil prices, events, chat)
- `/ws/airline/{id}` - Airline-specific updates (financials, notifications)

**Message Types**:
- `TICK` - Simulation advanced
- `MARKET_UPDATE` - Oil prices, demand shifts
- `AIRLINE_UPDATE` - Financial/reputation changes
- `CHAT` - Chat messages
- `NOTIFICATION` - User alerts

**Implementation Tasks**:
1. WebSocket client with reconnection logic (exponential backoff)
2. Typed message serialization/deserialization
3. Subscription/unsubscription API
4. Integration with React state (React Query or Zustand)
5. Toast notifications for real-time events

**Library Recommendation**: `reconnecting-websocket` (see original JS implementation)

## Testing Strategy

### Unit Tests
- **API Client**: Mock `fetch`, verify query parameters and error handling
- **Utility Functions**: Pure functions (if any)
- **Custom Hooks**: Future WebSocket hook with mock WebSocket

### Component Tests
- **SearchBox**: Debounce behavior, dropdown rendering, selection callback
- **MapView**: Marker rendering, popup content, selection highlighting
- **App**: Data loading, error handling, pagination

### Integration Tests (Future)
- E2E with Playwright or Cypress
- Test full user flows: search airport → select → view on map
- Test pagination and "Load More" behavior

### Current Coverage
- ✅ API client tests (6 tests)
- ✅ SearchBox tests (6 tests)
- ✅ MapView tests (4 tests, mocked react-leaflet)

**TODO**: Add App-level integration tests.

## Build & Deployment

### Development
```bash
npm run dev
```
- Vite dev server on port 5173
- HMR (Hot Module Replacement)
- Proxy `/api` and `/ws` to backend

### Production Build
```bash
npm run build
```
- TypeScript type checking
- Vite production build (minified, tree-shaken)
- Output in `dist/` directory

### Preview Production Build
```bash
npm run preview
```
- Serve production build locally for testing

### Deployment Options
1. **Static Hosting**: Serve `dist/` on CDN (Cloudflare Pages, Netlify, Vercel)
2. **Backend Integration**: Serve from Ktor static routes
3. **Nginx/Apache**: Reverse proxy API requests to backend

**CORS Note**: In production, ensure backend enables CORS or use reverse proxy to avoid CORS issues.

## Performance Considerations

### Current Optimizations
- Initial airport load limited to 200 markers
- Search debounced by 300ms
- Stale request cancellation with AbortController

### Future Optimizations
- **Marker Clustering**: Use `react-leaflet-markercluster` for > 500 airports
- **Virtualization**: Virtualized lists for large search results
- **Code Splitting**: Lazy load routes with React.lazy()
- **Image Optimization**: WebP images, lazy loading
- **Memoization**: `React.memo`, `useMemo`, `useCallback` where beneficial

## Accessibility

### Current Compliance
- ✅ Focus indicators on interactive elements
- ✅ ARIA labels on search input
- ✅ ARIA roles on search results dropdown
- ✅ Keyboard navigation for search results

### Future Improvements
- Skip-to-content link
- Keyboard shortcuts documentation
- Screen reader announcements for map state changes
- High contrast mode support

## Browser Support

### Target Browsers
- Modern evergreen browsers (Chrome, Firefox, Safari, Edge)
- ES2022 JavaScript features
- No IE11 support

### Polyfills
- None required (modern target only)

## Rationale for Tech Choices

### Why React + TypeScript?
- **React**: Mature ecosystem, large talent pool, excellent tooling
- **TypeScript**: Type safety reduces runtime errors, improves maintainability

### Why Vite?
- **Fast**: Near-instant HMR, fast builds
- **Modern**: Native ESM, optimal tree-shaking
- **Simple**: Minimal configuration, sensible defaults

### Why React Leaflet?
- **OpenStreetMap Compatible**: Requirement prohibits Google Maps
- **Mature**: Well-documented, large community
- **Lightweight**: Smaller bundle than MapLibre GL (though MapLibre has better performance for large datasets)

### Why Not MapLibre GL?
- Deferred for now; Leaflet is simpler for initial marker rendering
- **Future Migration**: Consider MapLibre if performance becomes an issue with large datasets (> 10K markers)

## Extension Points & TODOs

### High-Priority
- [ ] Marker clustering (react-leaflet-markercluster)
- [ ] WebSocket integration (real-time updates)
- [ ] User authentication UI
- [ ] Routing (React Router)

### Medium-Priority
- [ ] React Query for server state
- [ ] Theme system (light/dark mode)
- [ ] Internationalization (i18n)
- [ ] Analytics integration

### Low-Priority
- [ ] Service worker (offline support)
- [ ] Progressive Web App (PWA) features
- [ ] Advanced animations and transitions

## References

- [React Documentation](https://react.dev/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/handbook/intro.html)
- [Vite Guide](https://vitejs.dev/guide/)
- [React Leaflet Docs](https://react-leaflet.js.org/)
- [OpenStreetMap Tile Usage Policy](https://operations.osmfoundation.org/policies/tiles/)
- [Vitest Documentation](https://vitest.dev/)
- [React Testing Library](https://testing-library.com/docs/react-testing-library/intro/)

## Last Updated

2025-11-17
