#!/bin/bash
# ============================================================
# TradeX Database Initializer
# Usage: ./init.sh [mysql_user] [mysql_password]
#   Defaults: user=root, password=yv10989197
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

DB_USER="${1:-root}"
DB_PASS="${2:-yv10989197}"
DB_HOST="localhost"
DB_PORT="3306"

echo "============================================"
echo "  TradeX Database Initializer"
echo "============================================"
echo "Host : $DB_HOST:$DB_PORT"
echo "User : $DB_USER"
echo ""

# Check mysql is available
if ! command -v mysql &> /dev/null; then
  echo "ERROR: mysql client not found. Install MySQL and ensure it is on your PATH."
  exit 1
fi

# Test connection
echo "Testing connection..."
mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" -e "SELECT 1;" &> /dev/null
if [ $? -ne 0 ]; then
  echo "ERROR: Cannot connect to MySQL. Check your credentials."
  exit 1
fi
echo "Connection OK."
echo ""

# Drop and recreate database
echo "Dropping existing TradeX database (if any)..."
mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" -e "DROP DATABASE IF EXISTS TradeX;"
echo "Done."

# Run schema
echo ""
echo "Creating schema..."
mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" < "$SCRIPT_DIR/schema.sql"
if [ $? -ne 0 ]; then
  echo "ERROR: schema.sql failed."
  exit 1
fi
echo "Schema created."

# Run seed data
echo ""
echo "Loading seed data..."
mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" < "$SCRIPT_DIR/data.sql"
if [ $? -ne 0 ]; then
  echo "ERROR: data.sql failed."
  exit 1
fi
echo "Seed data loaded."

echo ""
echo "============================================"
echo "  TradeX database initialized successfully."
echo "============================================"
echo ""
echo "Default accounts:"
echo "  admin@tradex.com   / admin123  (Admin)"
echo "  john.doe@email.com / (bcrypt-hashed)"
echo "  jane.smith@email.com / (bcrypt-hashed)"
