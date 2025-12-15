import './assets/main.css'

import { createApp } from 'vue'
import  { extend } from '@tresjs/core'
import * as THREE from 'three'
import pinia from '@/stores/pinia'

import App from './App.vue'
import router from './router'

// Registriert alle Three.js Objekte für TresJS (wichtig für 3D-Komponenten)
extend(THREE)
const app = createApp(App)

app.use(pinia)
app.use(router)

app.mount('#app')
