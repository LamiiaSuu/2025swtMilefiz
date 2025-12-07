import { createRouter, createWebHistory } from 'vue-router'
import GameView from '../views/GameView.vue'
import GameStartView from '@/views/GameStartView.vue'

const routes = [
  {
    path: '/',               // The initial route
    name: 'game-start',
    component: GameStartView,
  },
  {
    path: '/game',           // The actual game page
    name: 'game',
    component: GameView,
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
