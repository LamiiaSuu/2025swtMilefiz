import { createRouter, createWebHistory } from 'vue-router'
import GameView from '../views/GameView.vue'
import HomeView from '@/views/HomeView.vue'
import LobbyListView from '@/views/LobbyListView.vue'

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
      component: HomeView
    },
    {
      path: '/lobbylist',
      name: 'Lobbylist',
      component: LobbyListView
    }
  ],
})

export default router
