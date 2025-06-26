#!/bin/bash
# This script is the only command you need to run to start the server correctly.
# It ensures a clean environment, builds the application, and starts it with its database.

# Exit immediately if a command exits with a non-zero status.
set -e

echo "--- Stopping any running Docker containers... ---"
# Stop all running containers silently
docker-compose down

# Stop and remove the old standalone postgres container if it's running
docker stop my-postgres > /dev/null 2>&1 && docker rm my-postgres > /dev/null 2>&1

# Kill any process that might be running on port 8080
lsof -ti:8080 | xargs kill -9 > /dev/null 2>&1

echo "Cleanup complete."
echo ""
echo "Step 2: Packaging the application... (This might take a moment)"

# Set Maven options for memory
export MAVEN_OPTS="-Xmx1024m"

# Build the project using Maven
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "ERROR: Maven build failed. Please check the output above."
    exit 1
fi

echo "Build successful."
echo ""
echo "Step 3: Building and launching the Docker containers..."
echo "The application will be ready when you see logs from 'spring-app-container'."
echo "To stop the server, press Ctrl+C in this terminal."
echo "-----------------------------------------------------------------"
echo ""

# Build new images and start the application and database containers
docker-compose up --build 