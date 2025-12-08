import { createRouter, createWebHistory } from 'vue-router'
import GameView from '../views/GameView.vue'
import HomeView from '@/views/HomeView.vue'
import GameStartView from '@/views/GameStartView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'game',
      component: GameView,
    },
    {
      path: '/home',
      name: 'Homepage',
      component: HomeView,
    },
    {
      path: '/spielStart',
      name: 'game-start',
      component: GameStartView,
    }
  ],
})

export default router
