#!/bin/bash
set -e

echo "🚀 Setting up Global Airline League Development Environment"
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    echo "   Visit: https://docs.docker.com/get-docker/"
    exit 1
fi

echo "✅ Docker is installed"
echo ""

# Check if port 5432 is already in use
if lsof -Pi :5432 -sTCP:LISTEN -t >/dev/null 2>&1 || netstat -an 2>/dev/null | grep -q ':5432.*LISTEN' || ss -ltn 2>/dev/null | grep -q ':5432'; then
    echo "⚠️  Port 5432 is already in use"
    echo ""
    echo "You have two options:"
    echo ""
    echo "Option 1: Use existing PostgreSQL (recommended if you already have PostgreSQL running)"
    echo "  1. Ensure PostgreSQL is running"
    echo "  2. Create database: psql -U postgres -c \"CREATE DATABASE gal;\""
    echo "  3. Create user: psql -U postgres -c \"CREATE USER gal WITH PASSWORD 'gal';\""
    echo "  4. Grant privileges: psql -U postgres -c \"GRANT ALL PRIVILEGES ON DATABASE gal TO gal;\""
    echo "  5. Update .env if needed (it will be created with default localhost:5432 settings)"
    echo ""
    echo "Option 2: Stop existing PostgreSQL and use Docker"
    echo "  Linux: sudo systemctl stop postgresql"
    echo "  macOS: brew services stop postgresql"
    echo "  Then run this script again"
    echo ""
    read -p "Continue with Option 1 (use existing PostgreSQL)? [y/N] " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ Setup cancelled. Please stop the existing PostgreSQL service and run this script again."
        exit 1
    fi
    
    echo "✅ Using existing PostgreSQL on port 5432"
    USING_EXISTING_POSTGRES=true
else
    # Check if PostgreSQL container already exists
    if docker ps -a | grep -q gal-postgres; then
        echo "📦 PostgreSQL container already exists"
        
        # Check if it's running
        if docker ps | grep -q gal-postgres; then
            echo "✅ PostgreSQL is running"
        else
            echo "🔄 Starting existing PostgreSQL container..."
            docker start gal-postgres
            sleep 3
        fi
    else
        echo "📦 Creating PostgreSQL container..."
        docker run -d \
            --name gal-postgres \
            -e POSTGRES_USER=gal \
            -e POSTGRES_PASSWORD=gal \
            -e POSTGRES_DB=gal \
            -p 5432:5432 \
            postgres:14
        
        echo "⏳ Waiting for PostgreSQL to be ready..."
        sleep 5
    fi
    USING_EXISTING_POSTGRES=false
fi

# Wait for PostgreSQL to be ready
if [ "$USING_EXISTING_POSTGRES" = true ]; then
    # Test connection to existing PostgreSQL
    if psql -U gal -d gal -c "SELECT 1;" >/dev/null 2>&1 || PGPASSWORD=gal psql -h localhost -U gal -d gal -c "SELECT 1;" >/dev/null 2>&1; then
        echo "✅ PostgreSQL connection successful"
    else
        echo "⚠️  Could not connect to existing PostgreSQL"
        echo "    Please ensure the database 'gal' exists and user 'gal' has access"
        echo "    You may need to run:"
        echo "      psql -U postgres -c \"CREATE DATABASE gal;\""
        echo "      psql -U postgres -c \"CREATE USER gal WITH PASSWORD 'gal';\""
        echo "      psql -U postgres -c \"GRANT ALL PRIVILEGES ON DATABASE gal TO gal;\""
        exit 1
    fi
else
    # Wait for Docker PostgreSQL to be ready
    until docker exec gal-postgres pg_isready -U gal 2>/dev/null; do
        echo "⏳ Waiting for PostgreSQL..."
        sleep 2
    done
    echo "✅ PostgreSQL is ready"
fi
echo ""

# Check if .env file exists
if [ ! -f .env ]; then
    echo "📝 Creating .env file..."
    cp .env.example .env 2>/dev/null || cat > .env << EOF
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/gal
DB_USER=gal
DB_PASSWORD=gal

# API Server Configuration
PORT=8080

# Simulation Configuration
TICK_INTERVAL_SECONDS=5
EOF
    echo "✅ .env file created"
else
    echo "✅ .env file already exists"
fi

echo ""
echo "🔨 Building backend..."
./gradlew build -x test

echo ""
echo "🌱 Starting backend to run migrations..."
# Start backend in background to run migrations, then stop it
timeout 15 ./gradlew :backend:api:run 2>&1 | grep -E "Migrating|Successfully applied|Application started" || true

echo ""
echo "📊 Seeding database with test airports..."
if [ "$USING_EXISTING_POSTGRES" = true ]; then
    # Use psql directly for existing PostgreSQL
    PGPASSWORD=gal psql -h localhost -U gal -d gal < docs/dev/seed_airports.sql 2>&1 | grep -E "INSERT|rows" || echo "Airports already seeded"
else
    # Use docker exec for containerized PostgreSQL
    docker exec -i gal-postgres psql -U gal -d gal < docs/dev/seed_airports.sql 2>&1 | grep -E "INSERT|rows" || echo "Airports already seeded"
fi

echo ""
echo "✅ Development environment setup complete!"
echo ""
echo "Next steps:"
echo "  1. Start the backend:  ./gradlew :backend:api:run"
echo "  2. In another terminal, start the frontend:"
echo "     cd frontend && npm install && npm run dev"
echo "  3. Open http://localhost:5173 in your browser"
echo ""
echo "To stop PostgreSQL: docker stop gal-postgres"
echo "To remove PostgreSQL: docker stop gal-postgres && docker rm gal-postgres"
