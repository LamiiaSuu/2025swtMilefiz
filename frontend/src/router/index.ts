import { createRouter, createWebHistory } from 'vue-router'
import GameView from '../views/GameView.vue'
import HomeView from '@/views/HomeView.vue'
import NewGameView from '@/views/NewGameView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Homepage',
      component: HomeView,
    },
    {
      path: '/game',
      name: 'game',
      component: GameView,
    },
    {
      path: '/gameStart',
      name: 'game-start',
      component: NewGameView,
    }
  ],
})

export default router
