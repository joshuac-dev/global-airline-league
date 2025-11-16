# Ubuntu Server Installation Guide

This guide walks you through setting up the Global Airline League development environment on a fresh Ubuntu Server installation (tested on Ubuntu 22.04 LTS and 24.04 LTS).

## Prerequisites

- Ubuntu Server 22.04 LTS or 24.04 LTS (minimal installation is sufficient)
- Root or sudo access
- Internet connection

## Step 1: Update System Packages

```bash
sudo apt update
sudo apt upgrade -y
```

## Step 2: Install Java Development Kit (JDK 17)

The project requires JDK 17 or higher.

```bash
# Install OpenJDK 17
sudo apt install -y openjdk-17-jdk

# Verify installation
java -version
javac -version
```

Expected output should show Java version 17.x.x.

## Step 3: Install Git

```bash
sudo apt install -y git

# Verify installation
git --version
```

## Step 4: Install PostgreSQL (Optional)

PostgreSQL is optional for initial development (the `/health` endpoint doesn't require it), but you'll need it for full functionality later.

```bash
# Install PostgreSQL 14 or higher
sudo apt install -y postgresql postgresql-contrib

# Start and enable PostgreSQL service
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Verify installation
sudo systemctl status postgresql
```

### Configure PostgreSQL

```bash
# Switch to postgres user
sudo -u postgres psql

# Create database and user (run these commands in psql prompt)
CREATE DATABASE gal;
CREATE USER gal WITH PASSWORD 'gal';
GRANT ALL PRIVILEGES ON DATABASE gal TO gal;
\q

# Test connection
psql -U gal -d gal -h localhost -W
# Enter password: gal
```

To allow local connections, edit PostgreSQL configuration:

```bash
# Edit pg_hba.conf
sudo nano /etc/postgresql/14/main/pg_hba.conf

# Add this line (if not already present):
# local   all             gal                                     md5

# Restart PostgreSQL
sudo systemctl restart postgresql
```

## Step 5: Clone the Repository

```bash
# Create a directory for projects (optional)
mkdir -p ~/projects
cd ~/projects

# Clone the repository
git clone https://github.com/joshuac-dev/global-airline-league.git
cd global-airline-league
```

## Step 6: Build the Project

The project includes a Gradle wrapper, so you don't need to install Gradle separately.

```bash
# Make gradlew executable (if needed)
chmod +x gradlew

# Build the project
./gradlew build
```

The first build will take several minutes as Gradle downloads dependencies.

## Step 7: Run the Application

### Start the API Server

```bash
./gradlew :backend:api:run
```

The server will start on `http://localhost:8080` by default.

### Test the Health Endpoint

Open a new terminal session and run:

```bash
curl http://localhost:8080/health
```

Expected response:
```json
{"status":"ok"}
```

To stop the server, press `Ctrl+C` in the terminal where it's running.

## Step 8: Import Airport Data (Optional)

To enable the airport-related API endpoints with real data, you can import airport information from the OurAirports dataset.

### Download the Dataset

```bash
# Download the OurAirports CSV
curl -o airports.csv https://ourairports.com/data/airports.csv
```

### Run the Importer

```bash
# Set environment variables
export DB_URL="jdbc:postgresql://localhost:5432/gal"
export DB_USER="gal"
export DB_PASSWORD="gal"
export IMPORT_AIRPORTS_CSV="$(pwd)/airports.csv"

# Run the import
./gradlew :backend:jobs:importAirports
```

The import process will:
- Read the CSV file in streaming mode (memory efficient)
- Skip rows with missing essential fields
- Insert airports in batches (default 1000 rows per batch)
- Log progress every 5,000 rows
- Complete in ~10-30 seconds for the full dataset (~70,000 airports)

### Verify the Import

Start the API server and test the airport endpoints:

```bash
# Start the server (in a separate terminal)
./gradlew :backend:api:run

# Test listing airports
curl 'http://localhost:8080/api/airports?limit=5'

# Test searching for an airport
curl 'http://localhost:8080/api/search/airports?q=heathrow'

# Test filtering by country
curl 'http://localhost:8080/api/airports?country=US&limit=5'
```

For more details, see the [Airport Import Guide](./dev/airport-import.md).

## Step 9: Configure Environment Variables (Optional)

For production or custom configurations, create a `.env` file or export variables:

```bash
# Set custom port
export PORT=8080

# Set database connection (when database is required)
export DB_URL="jdbc:postgresql://localhost:5432/gal"
export DB_USER="gal"
export DB_PASSWORD="gal"

# Set simulation tick interval
export TICK_INTERVAL_SECONDS=5
```

Alternatively, create a systemd service file for production deployment.

## Troubleshooting

### Issue: "Permission denied" when running gradlew

**Solution:**
```bash
chmod +x gradlew
```

### Issue: "Unable to access jarfile gradle/wrapper/gradle-wrapper.jar"

**Solution:**
```bash
# The Gradle wrapper JAR is missing. Regenerate it:
gradle wrapper --gradle-version 8.10.2

# Or if you don't have gradle installed globally, download from GitHub:
curl -L -o gradle/wrapper/gradle-wrapper.jar \
  https://github.com/gradle/gradle/raw/v8.10.2/gradle/wrapper/gradle-wrapper.jar

# Make sure gradlew is executable
chmod +x gradlew

# Try building again
./gradlew build
```

### Issue: Java version mismatch

**Solution:**
```bash
# Check available Java versions
update-java-alternatives --list

# Set default Java version
sudo update-alternatives --config java
sudo update-alternatives --config javac
```

### Issue: Port 8080 already in use

**Solution:**
```bash
# Find process using port 8080
sudo lsof -i :8080

# Kill the process (replace PID with actual process ID)
kill -9 <PID>

# Or use a different port
export PORT=8081
./gradlew :backend:api:run
```

### Issue: PostgreSQL connection refused

**Solution:**
```bash
# Check PostgreSQL status
sudo systemctl status postgresql

# If not running, start it
sudo systemctl start postgresql

# Check PostgreSQL is listening
sudo netstat -plntu | grep postgres
```

### Issue: Out of memory during build

**Solution:**
```bash
# Increase Gradle memory
export GRADLE_OPTS="-Xmx2048m -XX:MaxMetaspaceSize=512m"
./gradlew clean build
```

## Running Tests

```bash
# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew :backend:api:test
./gradlew :backend:core:test
```

## Development Tools (Optional)

### Install a Text Editor

**Option 1: Nano (already installed)**
```bash
nano <filename>
```

**Option 2: Vim**
```bash
sudo apt install -y vim
```

**Option 3: VS Code Server (for remote development)**
```bash
# Install VS Code Server
curl -fsSL https://code-server.dev/install.sh | sh

# Start VS Code Server
code-server

# Access via browser at http://localhost:8080
```

**Option 4: IntelliJ IDEA (requires desktop environment or X forwarding)**

### Install curl (if not already installed)

```bash
sudo apt install -y curl
```

### Install htop for monitoring

```bash
sudo apt install -y htop
```

## Production Deployment

For production deployments, consider:

1. **Systemd Service**: Create a systemd service file for automatic startup
2. **Reverse Proxy**: Use Nginx or Apache as a reverse proxy
3. **SSL/TLS**: Set up Let's Encrypt for HTTPS
4. **Firewall**: Configure UFW or iptables
5. **Monitoring**: Set up logging and monitoring tools
6. **Backup**: Implement database backup strategy

Example systemd service file (`/etc/systemd/system/gal.service`):

```ini
[Unit]
Description=Global Airline League API Server
After=network.target postgresql.service

[Service]
Type=simple
User=gal
WorkingDirectory=/home/gal/global-airline-league
ExecStart=/home/gal/global-airline-league/gradlew :backend:api:run
Restart=always
RestartSec=10

Environment="PORT=8080"
Environment="DB_URL=jdbc:postgresql://localhost:5432/gal"
Environment="DB_USER=gal"
Environment="DB_PASSWORD=your_secure_password"

[Install]
WantedBy=multi-user.target
```

Enable and start the service:
```bash
sudo systemctl daemon-reload
sudo systemctl enable gal.service
sudo systemctl start gal.service
sudo systemctl status gal.service
```

## Next Steps

1. Read [Architecture Documentation](./architecture.md) to understand the system design
2. Review [Feature Parity Checklist](./feature-parity-checklist.md) for development roadmap
3. Explore the codebase starting with `backend/api/src/main/kotlin/com/gal/api/Application.kt`
4. Set up your IDE with Kotlin support (IntelliJ IDEA recommended)

## Additional Resources

- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Ktor Documentation](https://ktor.io/docs/)
- [Gradle Documentation](https://docs.gradle.org/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Ubuntu Server Guide](https://ubuntu.com/server/docs)

## Getting Help

If you encounter issues not covered in this guide:

1. Check the project's [GitHub Issues](https://github.com/joshuac-dev/global-airline-league/issues)
2. Review the [Architecture Documentation](./architecture.md)
3. Consult the [PostgreSQL logs](sudo tail -f /var/log/postgresql/postgresql-14-main.log)
4. Check application logs in the terminal where the server is running
