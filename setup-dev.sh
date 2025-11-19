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

# Wait for PostgreSQL to be ready
until docker exec gal-postgres pg_isready -U gal 2>/dev/null; do
    echo "⏳ Waiting for PostgreSQL..."
    sleep 2
done

echo "✅ PostgreSQL is ready"
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
docker exec -i gal-postgres psql -U gal -d gal < docs/dev/seed_airports.sql 2>&1 | grep -E "INSERT|rows" || echo "Airports already seeded"

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
