#!/bin/bash

echo "🔍 Global Airline League - Environment Diagnostic"
echo "================================================"
echo ""

# Check if .env exists
echo "1. Checking .env configuration file..."
if [ -f .env ]; then
    echo "   ✅ .env file exists"
    echo "   Database URL: $(grep DB_URL .env | cut -d'=' -f2)"
else
    echo "   ❌ .env file NOT found"
    echo "   Action: Run ./setup-dev.sh to create it"
fi
echo ""

# Check if Docker is installed
echo "2. Checking Docker..."
if command -v docker &> /dev/null; then
    echo "   ✅ Docker is installed"
else
    echo "   ❌ Docker is NOT installed"
    echo "   Action: Install Docker or use manual PostgreSQL setup"
fi
echo ""

# Check if PostgreSQL container exists
echo "3. Checking PostgreSQL (Docker)..."
if docker ps -a 2>/dev/null | grep -q gal-postgres; then
    if docker ps 2>/dev/null | grep -q gal-postgres; then
        echo "   ✅ PostgreSQL container is RUNNING"
        # Test connection
        if docker exec gal-postgres pg_isready -U gal 2>/dev/null | grep -q "accepting connections"; then
            echo "   ✅ PostgreSQL is accepting connections"
        else
            echo "   ⚠️  PostgreSQL container running but not ready"
        fi
    else
        echo "   ⚠️  PostgreSQL container exists but is STOPPED"
        echo "   Action: Run 'docker start gal-postgres'"
    fi
else
    echo "   ℹ️  No Docker PostgreSQL container found"
    echo "   Checking system PostgreSQL..."
    
    # Check if system PostgreSQL is running
    if lsof -Pi :5432 -sTCP:LISTEN -t >/dev/null 2>&1 || netstat -an 2>/dev/null | grep -q ':5432.*LISTEN' || ss -ltn 2>/dev/null | grep -q ':5432'; then
        echo "   ✅ System PostgreSQL is running on port 5432"
        # Try to connect
        if PGPASSWORD=gal psql -h localhost -U gal -d gal -c "SELECT 1;" >/dev/null 2>&1; then
            echo "   ✅ Can connect to gal database"
        else
            echo "   ❌ Cannot connect to gal database"
            echo "   Action: Create database with:"
            echo "     sudo -u postgres psql -c \"CREATE DATABASE gal;\""
            echo "     sudo -u postgres psql -c \"CREATE USER gal WITH PASSWORD 'gal';\""
            echo "     sudo -u postgres psql -c \"GRANT ALL PRIVILEGES ON DATABASE gal TO gal;\""
        fi
    else
        echo "   ❌ No PostgreSQL found (Docker or system)"
        echo "   Action: Run ./setup-dev.sh"
    fi
fi
echo ""

# Check if database has airports
echo "4. Checking database content..."
if docker ps 2>/dev/null | grep -q gal-postgres; then
    AIRPORT_COUNT=$(docker exec gal-postgres psql -U gal -d gal -t -c "SELECT COUNT(*) FROM airports;" 2>/dev/null | tr -d ' ')
    if [ "$AIRPORT_COUNT" -gt 0 ] 2>/dev/null; then
        echo "   ✅ Database has $AIRPORT_COUNT airports"
    else
        echo "   ⚠️  Database has no airports or table doesn't exist"
        echo "   Action: Run migrations and seed data"
    fi
elif PGPASSWORD=gal psql -h localhost -U gal -d gal -c "SELECT 1;" >/dev/null 2>&1; then
    AIRPORT_COUNT=$(PGPASSWORD=gal psql -h localhost -U gal -d gal -t -c "SELECT COUNT(*) FROM airports;" 2>/dev/null | tr -d ' ')
    if [ "$AIRPORT_COUNT" -gt 0 ] 2>/dev/null; then
        echo "   ✅ Database has $AIRPORT_COUNT airports"
    else
        echo "   ⚠️  Database has no airports or table doesn't exist"
        echo "   Action: Run migrations and seed data"
    fi
else
    echo "   ⚠️  Cannot check - database not accessible"
fi
echo ""

# Check if backend is running
echo "5. Checking backend server..."
if curl -s http://localhost:8080/health 2>/dev/null | grep -q "ok"; then
    echo "   ✅ Backend is RUNNING and responding"
    
    # Test airports endpoint
    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/airports 2>/dev/null)
    if [ "$RESPONSE" = "200" ]; then
        echo "   ✅ Airports API is working (HTTP 200)"
    else
        echo "   ❌ Airports API returned HTTP $RESPONSE"
        echo "   Action: Check backend logs"
    fi
else
    echo "   ❌ Backend is NOT running"
    echo "   Action: Start backend with './gradlew :backend:api:run'"
fi
echo ""

# Check if frontend is running
echo "6. Checking frontend server..."
if curl -s http://localhost:5173 2>/dev/null | grep -q "vite"; then
    echo "   ✅ Frontend is RUNNING"
else
    echo "   ❌ Frontend is NOT running"
    echo "   Action: Start frontend with 'cd frontend && npm run dev'"
fi
echo ""

echo "================================================"
echo "📋 Summary"
echo "================================================"

# Provide recommendation
if [ -f .env ] && (docker ps 2>/dev/null | grep -q gal-postgres || (PGPASSWORD=gal psql -h localhost -U gal -d gal -c "SELECT 1;" >/dev/null 2>&1)); then
    echo "✅ Environment looks configured"
    echo ""
    if ! curl -s http://localhost:8080/health 2>/dev/null | grep -q "ok"; then
        echo "⚠️  Backend is not running. Start it with:"
        echo "   ./gradlew :backend:api:run"
    fi
    if ! curl -s http://localhost:5173 2>/dev/null | grep -q "vite"; then
        echo "⚠️  Frontend is not running. Start it with:"
        echo "   cd frontend && npm install && npm run dev"
    fi
else
    echo "❌ Setup incomplete. Please run:"
    echo "   ./setup-dev.sh"
    echo ""
    echo "   Then start the services:"
    echo "   1. Backend:  ./gradlew :backend:api:run"
    echo "   2. Frontend: cd frontend && npm install && npm run dev"
fi

echo ""
echo "For detailed troubleshooting, see: docs/TROUBLESHOOTING.md"
