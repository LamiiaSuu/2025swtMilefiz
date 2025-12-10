import { createRouter, createWebHistory } from 'vue-router'
import GameView from '../views/GameView.vue'
import HomeView from '@/views/HomeView.vue'
import JoinGameView from '@/views/JoinGameView.vue'
import SettingView from '@/views/SettingView.vue'
import MapEditorView from '@/views/MapEditorView.vue'

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
      path: '/settings',
      name: 'settings',
      component: SettingView,
    },
    {
      path: '/join-game',
      name: 'join-game',
      component: JoinGameView,
    },
        {
      path: '/map-editor',
      name: 'map-editor',
      component: MapEditorView,
    }
  ],
})

export default router
