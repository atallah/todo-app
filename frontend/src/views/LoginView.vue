<template>
  <div class="login-container animate-fade-in">
    <div class="card login-card">
      <h1>Welcome Back</h1>
      <p class="subtitle">Please sign in to manage your tasks</p>
      
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">Username</label>
          <input 
            type="text" 
            id="username" 
            v-model="username" 
            placeholder="user"
            required
          />
        </div>
        
        <div class="form-group">
          <label for="password">Password</label>
          <input 
            type="password" 
            id="password" 
            v-model="password" 
            placeholder="password"
            required
          />
        </div>
        
        <button type="submit" class="btn btn-primary w-full" :disabled="isLoading">
          {{ isLoading ? 'Signing in...' : 'Sign In' }}
        </button>
        
        <p v-if="error" class="error-msg">{{ error }}</p>
        
        <div class="auth-footer">
          Don't have an account? <router-link to="/register">Create one</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

const router = useRouter();
const username = ref('user'); // Default for demo
const password = ref('password'); // Default for demo
const isLoading = ref(false);
const error = ref('');

const handleLogin = async () => {
  isLoading.value = true;
  error.value = '';
  
  try {
    // Attempt a simple call to verify credentials
    const credentials = btoa(`${username.value}:${password.value}`);
    await axios.get('http://localhost:8080/api/todos', {
      headers: { 'Authorization': `Basic ${credentials}` }
    });
    
    // If successful, store credentials
    localStorage.setItem('isLoggedIn', 'true');
    localStorage.setItem('username', username.value);
    localStorage.setItem('password', password.value);
    
    router.push('/todos');
  } catch (err) {
    error.value = 'Invalid username or password';
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: 1rem;
}

.login-card {
  width: 100%;
  max-width: 400px;
}

h1 {
  text-align: center;
  margin-bottom: 0.5rem;
}

.subtitle {
  text-align: center;
  color: var(--text-muted);
  margin-bottom: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-muted);
}

.w-full {
  width: 100%;
}

.error-msg {
  color: var(--danger-color);
  text-align: center;
  margin-top: 1rem;
  font-size: 0.875rem;
}
</style>
