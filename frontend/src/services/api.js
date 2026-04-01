import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  withCredentials: true
});

// Request interceptor to add dynamic Basic Auth headers
api.interceptors.request.use(config => {
  const username = localStorage.getItem('username');
  const password = localStorage.getItem('password');
  
  if (username && password) {
    const credentials = btoa(`${username}:${password}`);
    config.headers.Authorization = `Basic ${credentials}`;
  }
  
  return config;
});

export const getTodos = () => api.get('/todos');
export const createTodo = (todo) => api.post('/todos', todo);
export const updateTodo = (id, todo) => api.put(`/todos/${id}`, todo);
export const deleteTodo = (id) => api.delete(`/todos/${id}`);

export default api;
