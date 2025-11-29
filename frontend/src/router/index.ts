import { createRouter, createWebHistory } from 'vue-router'
import { useUrlLobbyStore } from '@/stores/urlLobbyStore'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),
    },
    {
      path: '/lobby/:lobbyid',
      redirect: (to) => {
        // lese lobbyid aus url parametern und setze urlLobbyId in UrlLobbyStore, dann weiterleitung an home
        const id: string | undefined = (to.params.lobbyid as string | undefined)
        if (id)
          if (id) useUrlLobbyStore().setUrlLobbyId(id)
        return { name: 'home', replace: true }
      },
    },
  ],
})

export default router
