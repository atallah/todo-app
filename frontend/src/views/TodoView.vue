<template>
  <div class="container todo-container animate-fade-in">
    <header class="header">
      <div class="header-left">
        <h1>My Tasks</h1>
        <p class="subtitle">{{ todos.filter(t => !t.completed).length }} pending items</p>
      </div>
      <div class="header-right">
        <button class="btn btn-ghost" @click="handleLogout">Sign Out</button>
      </div>
    </header>

    <main class="card todo-card">
      <!-- Input Area -->
      <form @submit.prevent="handleAddTodo" class="add-todo-form">
        <input 
          type="text" 
          v-model="newTodoTitle" 
          placeholder="What needs to be done?" 
          class="todo-input"
          required
        />
        <button type="submit" class="btn btn-primary">Add Task</button>
      </form>

      <!-- Todo List -->
      <div v-if="isLoading" class="loading-state">
        <div class="spinner"></div>
        <p>Loading your tasks...</p>
      </div>

      <div v-else-if="todos.length === 0" class="empty-state">
        <span class="icon">📝</span>
        <h3>No tasks yet</h3>
        <p>Your task list is empty. Add a new task to get started!</p>
      </div>

      <ul v-else class="todo-list">
        <li v-for="todo in todos" :key="todo.id" class="todo-item" :class="{ completed: todo.completed }">
          <div class="todo-check" @click="toggleTodo(todo)">
            <div class="checkbox">
              <span v-if="todo.completed">✓</span>
            </div>
            <div class="todo-content">
              <span class="todo-title">{{ todo.title }}</span>
            </div>
          </div>
          <button class="btn btn-ghost delete-btn" @click="handleDeleteTodo(todo.id)">
            Delete
          </button>
        </li>
      </ul>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import * as api from '../services/api';

const router = useRouter();
const todos = ref([]);
const newTodoTitle = ref('');
const isLoading = ref(true);

const fetchTodos = async () => {
  try {
    isLoading.value = true;
    const response = await api.getTodos();
    todos.value = response.data;
  } catch (error) {
    console.error('Error fetching todos:', error);
  } finally {
    isLoading.value = false;
  }
};

const handleAddTodo = async () => {
  if (!newTodoTitle.value.trim()) return;
  
  try {
    const response = await api.createTodo({
      title: newTodoTitle.value,
      completed: false
    });
    todos.value.unshift(response.data);
    newTodoTitle.value = '';
  } catch (error) {
    console.error('Error adding todo:', error);
  }
};

const toggleTodo = async (todo) => {
  try {
    const updatedTodo = { ...todo, completed: !todo.completed };
    const response = await api.updateTodo(todo.id, updatedTodo);
    const index = todos.value.findIndex(t => t.id === todo.id);
    if (index !== -1) {
      todos.value[index] = response.data;
    }
  } catch (error) {
    console.error('Error updating todo:', error);
  }
};

const handleDeleteTodo = async (id) => {
  try {
    await api.deleteTodo(id);
    todos.value = todos.value.filter(t => t.id !== id);
  } catch (error) {
    console.error('Error deleting todo:', error);
  }
};

const handleLogout = () => {
  localStorage.removeItem('isLoggedIn');
  localStorage.removeItem('username');
  localStorage.removeItem('password');
  router.push('/login');
};

onMounted(fetchTodos);
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.header h1 {
  margin-bottom: 0;
}

.subtitle {
  color: var(--text-muted);
  font-size: 0.875rem;
}

.add-todo-form {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.todo-input {
  flex: 1;
  margin-bottom: 0 !important;
}

.todo-list {
  list-style: none;
}

.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid var(--border-color);
  transition: background-color 0.2s;
  border-radius: 0.5rem;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-item:hover {
  background: rgba(255, 255, 255, 0.02);
}

.todo-check {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex: 1;
  cursor: pointer;
}

.checkbox {
  width: 1.5rem;
  height: 1.5rem;
  border: 2px solid var(--border-color);
  border-radius: 0.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--success-color);
  font-weight: bold;
}

.todo-item.completed .checkbox {
  background: var(--success-color);
  border-color: var(--success-color);
  color: white;
}

.todo-item.completed .todo-title {
  text-decoration: line-through;
  color: var(--text-muted);
}

.todo-title {
  font-weight: 500;
}

.delete-btn {
  opacity: 0;
  transition: opacity 0.2s;
}

.todo-item:hover .delete-btn {
  opacity: 1;
}

.loading-state, .empty-state {
  text-align: center;
  padding: 3rem 0;
}

.icon {
  font-size: 3rem;
  display: block;
  margin-bottom: 1rem;
}

.spinner {
  width: 2rem;
  height: 2rem;
  border: 3px solid var(--border-color);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
