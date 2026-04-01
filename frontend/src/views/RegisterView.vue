<template>
  <div class="login-container animate-fade-in">
    <div class="card login-card">
      <h1>Create Account</h1>
      <p class="subtitle">Join us to stay organized</p>
      
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="username">Username</label>
          <input 
            type="text" 
            id="username" 
            v-model="username" 
            placeholder="Choose a username"
            required
          />
        </div>
        
        <div class="form-group">
          <label for="password">Password</label>
          <input 
            type="password" 
            id="password" 
            v-model="password" 
            placeholder="Choose a password"
            required
          />
        </div>
        
        <button type="submit" class="btn btn-primary w-full" :disabled="isLoading">
          {{ isLoading ? 'Creating account...' : 'Register' }}
        </button>
        
        <p v-if="error" class="error-msg">{{ error }}</p>
        <p v-if="success" class="success-msg">{{ success }}</p>
        
        <div class="auth-footer">
          Already have an account? <router-link to="/login">Sign In</router-link>
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
const username = ref('');
const password = ref('');
const isLoading = ref(false);
const error = ref('');
const success = ref('');

const handleRegister = async () => {
  isLoading.value = true;
  error.value = '';
  success.value = '';
  
  try {
    await axios.post('http://localhost:8080/api/register', {
      username: username.value,
      password: password.value
    });
    
    success.value = 'Registration successful! Redirecting to login...';
    setTimeout(() => {
      router.push('/login');
    }, 2000);
  } catch (err) {
    error.value = err.response?.data?.message || 'Registration failed. Please try again.';
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

.success-msg {
  color: var(--success-color);
  text-align: center;
  margin-top: 1rem;
  font-size: 0.875rem;
}

.auth-footer {
  text-align: center;
  margin-top: 1.5rem;
  font-size: 0.875rem;
  color: var(--text-muted);
}

.auth-footer a {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 600;
}

.auth-footer a:hover {
  text-decoration: underline;
}
</style>
