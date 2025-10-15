#!/bin/bash

# Quick Database Access Script for SkillSync
# Usage: ./db.sh [command]

export PATH="/opt/homebrew/opt/postgresql@15/bin:$PATH"

case "$1" in
    "connect"|"")
        echo "🗄️  Connecting to SkillSync database..."
        psql skillsync
        ;;
    "users")
        echo "👥 All Users:"
        psql -d skillsync -c "SELECT name, email, \"current_role\", \"target_role\", skills FROM users;"
        ;;
    "tables")
        echo "📊 All Tables:"
        psql -d skillsync -c "\dt"
        ;;
    "count")
        echo "📈 Database Statistics:"
        psql -d skillsync -c "SELECT COUNT(*) as total_users FROM users;"
        ;;
    "status")
        echo "✅ PostgreSQL Status:"
        brew services list | grep postgresql
        echo ""
        echo "📊 Database Info:"
        psql -d skillsync -c "SELECT current_database() as database, current_user as user, version() as version;"
        ;;
    "help")
        echo "SkillSync Database Helper"
        echo ""
        echo "Usage: ./db.sh [command]"
        echo ""
        echo "Commands:"
        echo "  connect (or no argument) - Open PostgreSQL prompt"
        echo "  users                   - Show all users"
        echo "  tables                  - List all tables"
        echo "  count                   - Show user count"
        echo "  status                  - Check PostgreSQL status"
        echo "  help                    - Show this help"
        echo ""
        echo "Examples:"
        echo "  ./db.sh              # Connect to database"
        echo "  ./db.sh users        # View all users"
        echo "  ./db.sh status       # Check if PostgreSQL is running"
        ;;
    *)
        echo "Unknown command: $1"
        echo "Run './db.sh help' for usage information"
        exit 1
        ;;
esac
