#!/bin/bash

set -euo pipefail

# Generate JWT Secret (64 hex characters)
JWT_SECRET=$(openssl rand -hex 32)

# Generate Admin Key (32+ URL-safe characters)
ADMIN_KEY=$(openssl rand -base64 24 | tr -d '\n' | tr -d '=' | tr '/+' '_-')

# Generate PostgreSQL Password
DB_PASSWORD=$(openssl rand -base64 24 | tr -d '\n' | tr -d '=')

cat > .env <<EOT
# Shared Database Configuration
POSTGRES_USER=akentech
POSTGRES_PASSWORD=${DB_PASSWORD}

# Service Database Names
INCOME_DB_NAME=income_db
EXPENSE_DB_NAME=expense_db
INVESTMENT_DB_NAME=investment_db
TRANSACTION_DB_NAME=transaction_db
USER_DB_NAME=user_db

# JWT Configuration
JWT_SECRET=${JWT_SECRET}

# Admin Key for privileged registration flows
ADMIN_KEY=${ADMIN_KEY}

# CORS Allowed Origins
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000

# Environment Profile
SPRING_PROFILES_ACTIVE=docker
EOT

echo "Secrets generated and saved to .env file"
echo "Never commit .env file to version control!"
echo "Save these secrets in your password manager:"
echo "JWT_SECRET=${JWT_SECRET}"
echo "ADMIN_KEY=${ADMIN_KEY}"
echo "POSTGRES_PASSWORD=${DB_PASSWORD}"
