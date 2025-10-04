#!/bin/bash

echo ">>> Stopping old container (if exists)..."
docker-compose down

echo ">>> Starting LocalStack..."
docker-compose up -d

echo ">>> LocalStack is running on http://localhost:4566"