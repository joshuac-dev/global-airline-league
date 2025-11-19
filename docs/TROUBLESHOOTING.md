# Troubleshooting Guide

This guide helps resolve common issues when running the Global Airline League application.

## "Failed to fetch airports: Internal Server Error"

### Symptoms
- Frontend shows red error banner: "Failed to fetch airports: Internal Server Error"
- Map page doesn't load airport markers
- Browser console shows 500 or 503 errors when fetching `/api/airports`

### Root Cause
The backend cannot connect to the PostgreSQL database.

### Solution

**Step 1: Check if PostgreSQL is running**

```bash
# If using Docker (recommended)
docker ps | grep gal-postgres

# If using system PostgreSQL
pg_isready -U gal
```

If PostgreSQL is not running:

```bash
# Option A: Use the automated setup script
./setup-dev.sh

# Option B: Start Docker container manually
docker start gal-postgres

# Option C: Start system PostgreSQL
sudo systemctl start postgresql  # Linux
brew services start postgresql@14  # macOS
```

**Step 2: Verify database exists**

```bash
# Docker
docker exec -it gal-postgres psql -U gal -l

# System PostgreSQL
psql -U gal -l
```

You should see a database named `gal`. If not:

```bash
# Docker
docker exec -it gal-postgres psql -U postgres -c "CREATE DATABASE gal; GRANT ALL PRIVILEGES ON DATABASE gal TO gal;"

# System PostgreSQL
sudo -u postgres psql -c "CREATE DATABASE gal;"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE gal TO gal;"
```

**Step 3: Check .env file exists**

```bash
cat .env
```

Should show:
```
DB_URL=jdbc:postgresql://localhost:5432/gal
DB_USER=gal
DB_PASSWORD=gal
PORT=8080
```

If missing, create it:
```bash
cp .env.example .env
```

**Step 4: Verify backend can connect**

Restart the backend:
```bash
./gradlew :backend:api:run
```

Look for these log messages:
- ✅ `HikariPool-1 - Start completed.`
- ✅ `Successfully applied X migrations`
- ✅ `Application started`

If you see connection errors, check your database credentials in `.env`.

**Step 5: Seed airport data**

```bash
# Docker
docker exec -i gal-postgres psql -U gal -d gal < docs/dev/seed_airports.sql

# System PostgreSQL
psql -U gal -d gal < docs/dev/seed_airports.sql
```

You should see: `INSERT 0 15`

**Step 6: Test the API**

```bash
curl http://localhost:8080/api/airports
```

Should return JSON array with 15 airports.

**Step 7: Reload frontend**

Open http://localhost:5173 and you should see the map with airport markers.

---

## "Database unavailable" (503 Error)

### Symptoms
- API returns `{"error": "database unavailable"}`
- Backend logs: `Failed to initialize database`

### Solution
Follow steps 1-4 from the "Internal Server Error" section above.

---

## "Empty airports list" (200 but no data)

### Symptoms
- API returns `[]` (empty array)
- Map shows no markers
- No error messages

### Solution
The database is connected but has no data. Run the seed script:

```bash
# Docker
docker exec -i gal-postgres psql -U gal -d gal < docs/dev/seed_airports.sql

# System PostgreSQL  
psql -U gal -d gal < docs/dev/seed_airports.sql
```

---

## Frontend not connecting to backend

### Symptoms
- Browser console: `Failed to fetch` or `NetworkError`
- API requests to `/api/*` fail

### Solution

**Step 1: Verify backend is running**

```bash
curl http://localhost:8080/health
```

Should return: `{"status":"ok"}`

**Step 2: Check frontend proxy configuration**

The Vite dev server should proxy `/api` requests to `localhost:8080`. Verify:

```bash
cd frontend
cat vite.config.ts | grep -A5 proxy
```

Should show:
```typescript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
  },
```

**Step 3: Restart frontend**

```bash
cd frontend
npm run dev
```

---

## Docker container won't start

### Symptoms
- `docker start gal-postgres` fails
- Port 5432 already in use

### Solution

**Check if another PostgreSQL is using port 5432:**

```bash
lsof -i :5432
# or
netstat -an | grep 5432
```

**Option A: Stop system PostgreSQL**
```bash
sudo systemctl stop postgresql  # Linux
brew services stop postgresql@14  # macOS
```

**Option B: Use different port for Docker**
```bash
docker run -d --name gal-postgres \
  -e POSTGRES_USER=gal \
  -e POSTGRES_PASSWORD=gal \
  -e POSTGRES_DB=gal \
  -p 5433:5432 \
  postgres:14

# Update .env
DB_URL=jdbc:postgresql://localhost:5433/gal
```

---

## Tests failing

### Backend tests
```bash
# Run all tests
./gradlew test

# Run specific module
./gradlew :backend:api:test
./gradlew :backend:core:test
```

Common issue: Tests use in-memory stubs and shouldn't require a real database.

### Frontend tests
```bash
cd frontend
npm test
```

Common issue: Tests mock API calls and shouldn't require backend.

---

## Build failures

### Kotlin compilation errors

```bash
# Clean and rebuild
./gradlew clean build
```

### Frontend build errors

```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run build
```

---

## Getting Help

If none of these solutions work:

1. Check backend logs: Look for stack traces in the terminal running `./gradlew :backend:api:run`
2. Check browser console: Open DevTools (F12) and look for error messages
3. Verify versions:
   ```bash
   java -version  # Should be 17+
   node -v  # Should be 20+
   docker --version  # Should be 20+
   ```

4. Create a GitHub issue with:
   - Steps to reproduce
   - Error messages (backend logs + browser console)
   - Your OS and tool versions
