import { createRouter, createWebHistory } from 'vue-router'
import GameView from '../views/GameView.vue'
import GameStartView from '@/views/GameStartView.vue'

const routes = [
  {
    // Spiel Start Seite
    path: '/',
    name: 'game-start',
    component: GameStartView,
  },
  {
    // In-Game
    path: '/game',
    name: 'game',
    component: GameView,
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
