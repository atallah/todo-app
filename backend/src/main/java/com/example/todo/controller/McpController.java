package com.example.todo.controller;

import com.example.todo.model.McpRequest;
import com.example.todo.model.McpResponse;
import com.example.todo.model.Todo;
import com.example.todo.model.User;
import com.example.todo.repository.TodoRepository;
import com.example.todo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/mcp")
@CrossOrigin(origins = "*")
public class McpController {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @PostMapping
    public McpResponse handleMcpRequest(@RequestBody McpRequest request) {
        String method = request.getMethod();
        Object id = request.getId();

        try {
            if ("initialize".equals(method)) {
                return McpResponse.builder()
                        .jsonrpc("2.0")
                        .id(id)
                        .result(Map.of(
                                "protocolVersion", "2024-11-05",
                                "capabilities", Map.of("tools", Map.of()),
                                "serverInfo", Map.of("name", "Todo MCP Server", "version", "1.0.0")
                        ))
                        .build();
            } else if ("tools/list".equals(method)) {
                return McpResponse.builder()
                        .jsonrpc("2.0")
                        .id(id)
                        .result(Map.of("tools", getToolsList()))
                        .build();
            } else if ("tools/call".equals(method)) {
                String toolName = (String) request.getParams().get("name");
                Map<String, Object> arguments = (Map<String, Object>) request.getParams().get("arguments");
                return handleToolCall(toolName, arguments, id);
            } else {
                return McpResponse.builder()
                        .jsonrpc("2.0")
                        .id(id)
                        .error(Map.of("code", -32601, "message", "Method not found"))
                        .build();
            }
        } catch (Exception e) {
            return McpResponse.builder()
                    .jsonrpc("2.0")
                    .id(id)
                    .error(Map.of("code", -32603, "message", "Internal error: " + e.getMessage()))
                    .build();
        }
    }

    private List<Map<String, Object>> getToolsList() {
        return List.of(
                Map.of(
                        "name", "list_todos",
                        "description", "Retrieve all todo items for the current user.",
                        "inputSchema", Map.of("type", "object", "properties", Map.of())
                ),
                Map.of(
                        "name", "add_todo",
                        "description", "Add a new todo item.",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "title", Map.of("type", "string", "description", "The title of the todo item"),
                                        "description", Map.of("type", "string", "description", "The description of the todo item")
                                ),
                                "required", List.of("title")
                        )
                ),
                Map.of(
                        "name", "delete_todo",
                        "description", "Delete a todo item by ID.",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "id", Map.of("type", "integer", "description", "The ID of the todo item to delete")
                                ),
                                "required", List.of("id")
                        )
                ),
                Map.of(
                        "name", "complete_todo",
                        "description", "Mark a todo item as completed or incomplete.",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "id", Map.of("type", "integer", "description", "The ID of the todo item to mark"),
                                        "completed", Map.of("type", "boolean", "description", "Whether the todo item is completed")
                                ),
                                "required", List.of("id", "completed")
                        )
                )
        );
    }

    private McpResponse handleToolCall(String name, Map<String, Object> arguments, Object id) {
        User currentUser = getCurrentUser();
        Object toolResult = null;

        switch (name) {
            case "list_todos":
                toolResult = todoRepository.findByUser(currentUser);
                break;

            case "add_todo":
                String title = (String) arguments.get("title");
                String description = (String) arguments.get("description");
                Todo todo = new Todo();
                todo.setTitle(title);
                todo.setDescription(description);
                todo.setUser(currentUser);
                todo.setCompleted(false);
                toolResult = todoRepository.save(todo);
                break;

            case "delete_todo":
                Long deleteId = ((Number) arguments.get("id")).longValue();
                Optional<Todo> todoToDelete = todoRepository.findById(deleteId);
                if (todoToDelete.isPresent() && todoToDelete.get().getUser().getId().equals(currentUser.getId())) {
                    todoRepository.delete(todoToDelete.get());
                    toolResult = Map.of("status", "success", "message", "Todo deleted");
                } else {
                    return McpResponse.builder()
                            .jsonrpc("2.0")
                            .id(id)
                            .error(Map.of("code", -32602, "message", "Todo not found or not owned by user"))
                            .build();
                }
                break;

            case "complete_todo":
                Long completeId = ((Number) arguments.get("id")).longValue();
                Boolean completed = (Boolean) arguments.get("completed");
                Optional<Todo> todoToUpdate = todoRepository.findById(completeId);
                if (todoToUpdate.isPresent() && todoToUpdate.get().getUser().getId().equals(currentUser.getId())) {
                    Todo updatedTodo = todoToUpdate.get();
                    updatedTodo.setCompleted(completed);
                    toolResult = todoRepository.save(updatedTodo);
                } else {
                    return McpResponse.builder()
                            .jsonrpc("2.0")
                            .id(id)
                            .error(Map.of("code", -32602, "message", "Todo not found or not owned by user"))
                            .build();
                }
                break;

            default:
                return McpResponse.builder()
                        .jsonrpc("2.0")
                        .id(id)
                        .error(Map.of("code", -32602, "message", "Unknown tool: " + name))
                        .build();
        }

        return McpResponse.builder()
                .jsonrpc("2.0")
                .id(id)
                .result(Map.of("content", List.of(Map.of("type", "text", "text", formatResult(toolResult)))))
                .build();
    }

    private String formatResult(Object result) {
        // Simple string formatting for the AI to read
        if (result instanceof List) {
            List<Todo> todos = (List<Todo>) result;
            if (todos.isEmpty()) return "You have no todo items.";
            StringBuilder sb = new StringBuilder("Current todos:\n");
            for (Todo t : todos) {
                sb.append(String.format("- [%d] %s (Completed: %b)\n  %s\n", t.getId(), t.getTitle(), t.isCompleted(), t.getDescription() != null ? t.getDescription() : ""));
            }
            return sb.toString();
        } else if (result instanceof Todo) {
            Todo t = (Todo) result;
            return String.format("Successfully updated/added todo: [%d] %s", t.getId(), t.getTitle());
        } else if (result instanceof Map) {
            return ((Map<?, ?>) result).get("message").toString();
        }
        return String.valueOf(result);
    }
}
