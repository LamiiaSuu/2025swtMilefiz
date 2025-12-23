import { createRouter, createWebHistory } from 'vue-router'
import GameView from '../views/GameView.vue'
import HomeView from '@/views/HomeView.vue'
import NewGameView from '@/views/NewGameView.vue'
import JoinGameView from '@/views/JoinGameView.vue'
import SettingView from '@/views/SettingView.vue'
import MapEditorView from '@/views/MapEditorView.vue'
import NotFoundView from '@/views/NotFoundView.vue'

import pinia from '@/stores/pinia'
import { useMilefizStore } from '@/stores/milefizstore'

const { joinLobby } = useMilefizStore(pinia)

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
    },
    {
      path: '/join/:lobbyid',
      component: NewGameView,
      beforeEnter: async (to, from) => {
        // lobbyid aus parametern
        const id: string | undefined = (to.params.lobbyid as string | undefined)

        // warten bis lobby gejoint
        if (id) {
          console.log(`joine mit ${id}`);
          await joinLobby(id)
        }
        return { name: 'game-start', replace: true }
      },
    },
    {
    path: '/:pathMatch(.*)*',
    name: 'NotFoundView',
    component: NotFoundView,
    },
  ],
})

export default router
