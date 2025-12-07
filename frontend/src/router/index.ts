import { createRouter, createWebHistory } from 'vue-router'
import GameView from '../views/GameView.vue'
import GameStartView from '@/views/GameStartView.vue'

const routes = [
  {
    path: '/',
    name: 'game-start',
    component: GameStartView,
  },
  {
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
