import { createApp } from 'vue'
import './style.css'
import './styles/global.scss'
import './styles/element-plus.scss'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'

// Polyfill for sockjs-client
if (typeof window !== 'undefined') {
  (window as any).global = window;
}

const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.mount('#app')
