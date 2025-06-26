#!/bin/bash

# Startup script for Project3 Grocery Store Application
set -e

echo "🚀 Starting Project3 Grocery Store Application..."

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    echo "Visit: https://docs.docker.com/get-docker/"
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed. Please install Docker Compose first."
    echo "Visit: https://docs.docker.com/compose/install/"
    exit 1
fi

# Change to backend directory
cd backend

# Environment setup
print_status "Setting up environment..."

# Create .env file if it doesn't exist
if [ ! -f .env ]; then
    print_warning ".env file not found. Creating with default values..."
    cat > .env << EOF
# Database Configuration
DATABASE_URL=jdbc:postgresql://postgres:5432/project3
DATABASE_USERNAME=project3_user
DATABASE_PASSWORD=project3_password

# JWT Configuration
JWT_SECRET=veryLongAndSecureSecretKeyForJWTTokensAtLeast32CharactersLong
JWT_EXPIRATION_MS=86400000

# Email Configuration (optional - update with your credentials)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# AI Configuration (optional - update with your API keys)
GEMINI_API_KEY=your-gemini-api-key
GEMINI_PROJECT_ID=your-project-id

# Logging
LOG_LEVEL=INFO

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8081
EOF
    print_warning "Please update the .env file with your actual configuration values."
fi

# Stop any existing containers
print_status "Stopping any existing containers..."
docker-compose down --volumes --remove-orphans 2>/dev/null || true

# Clean up old images (optional)
if [ "$1" = "--clean" ]; then
    print_status "Cleaning up old Docker images..."
    docker system prune -f
    docker volume prune -f
fi

# Build and start the application
print_status "Building and starting the application..."
docker-compose up --build -d

# Wait for services to be healthy
print_status "Waiting for services to start..."

# Wait for PostgreSQL
print_status "Waiting for PostgreSQL to be ready..."
for i in {1..30}; do
    if docker-compose exec -T postgres pg_isready -U project3_user -d project3 > /dev/null 2>&1; then
        print_success "PostgreSQL is ready!"
        break
    fi
    echo -n "."
    sleep 2
done

# Wait for Spring Boot application
print_status "Waiting for Spring Boot application to be ready..."
for i in {1..60}; do
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        print_success "Spring Boot application is ready!"
        break
    fi
    echo -n "."
    sleep 3
done

# Show status
print_status "Checking service status..."
docker-compose ps

# Show logs for a few seconds
print_status "Recent application logs:"
docker-compose logs --tail=20 backend

echo ""
print_success "🎉 Application started successfully!"
echo ""
echo "📍 Service URLs:"
echo "   Backend API: http://localhost:8080"
echo "   Health Check: http://localhost:8080/actuator/health"
echo "   API Documentation: http://localhost:8080/swagger-ui.html (if configured)"
echo ""
echo "🗄️  Database:"
echo "   PostgreSQL: localhost:5432"
echo "   Database: project3"
echo "   Username: project3_user"
echo ""
echo "📱 iOS App Configuration:"
echo "   Update Info.plist API_BASE_URL to: http://localhost:8080/api"
echo ""
echo "🔧 Useful commands:"
echo "   View logs: docker-compose logs -f backend"
echo "   Stop app: docker-compose down"
echo "   Restart: ./start.sh"
echo "   Clean restart: ./start.sh --clean"
echo ""

# Optional: Open browser to health check
if command -v open &> /dev/null; then
    read -p "Open health check in browser? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        open http://localhost:8080/actuator/health
    fi
fi 