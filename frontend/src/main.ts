import { createApp } from 'vue'
import './style.css'
import './styles/global.scss'
import './styles/element-plus.scss'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'

const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.mount('#app')
