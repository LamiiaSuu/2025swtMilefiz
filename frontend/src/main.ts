import './assets/main.css'

import { createApp } from 'vue'
import  { extend } from '@tresjs/core'
import * as THREE from 'three'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

extend(THREE)
const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
