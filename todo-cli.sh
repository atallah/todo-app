#!/bin/bash

# Configuration
SERVER_URL="http://localhost:8080/api/mcp"
USERNAME="testuser"
PASSWORD="password"

# Function to send JSON-RPC request to MCP server
send_mcp_request() {
    local method="$1"
    local params="$2"
    local id="${3:-1}"

    local json_payload="{\"jsonrpc\": \"2.0\", \"method\": \"$method\", \"params\": $params, \"id\": $id}"
    
    curl -s -u "$USERNAME:$PASSWORD" -X POST "$SERVER_URL" \
         -H "Content-Type: application/json" \
         -d "$json_payload" | grep -o '"text":"[^"]*"' | cut -d'"' -f4 | sed 's/\\n/\n/g'
}

# Commands
case "$1" in
    list)
        echo "Listing todos..."
        send_mcp_request "tools/call" '{"name": "list_todos", "arguments": {}}'
        ;;
    add)
        if [ -z "$2" ]; then
            echo "Usage: $0 add <title> [description]"
            exit 1
        fi
        title="$2"
        description="${3:-}"
        echo "Adding todo: $title..."
        send_mcp_request "tools/call" "{\"name\": \"add_todo\", \"arguments\": {\"title\": \"$title\", \"description\": \"$description\"}}"
        ;;
    delete)
        if [ -z "$2" ]; then
            echo "Usage: $0 delete <id>"
            exit 1
        fi
        id="$2"
        echo "Deleting todo #$id..."
        send_mcp_request "tools/call" "{\"name\": \"delete_todo\", \"arguments\": {\"id\": $id}}"
        ;;
    complete)
        if [ -z "$2" ]; then
            echo "Usage: $0 complete <id> [true|false]"
            exit 1
        fi
        id="$2"
        completed="${3:-true}"
        echo "Updating todo #$id (completed: $completed)..."
        send_mcp_request "tools/call" "{\"name\": \"complete_todo\", \"arguments\": {\"id\": $id, \"completed\": $completed}}"
        ;;
    help|*)
        echo "Todo App CLI (via MCP Server)"
        echo "Usage: $0 <command> [args]"
        echo ""
        echo "Commands:"
        echo "  list                      List all todos"
        echo "  add <title> <desc>        Add a new todo"
        echo "  delete <id>               Delete a todo by ID"
        echo "  complete <id> <true|false> Mark a todo as complete/incomplete"
        ;;
esac
